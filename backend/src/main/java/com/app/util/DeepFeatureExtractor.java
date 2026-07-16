package com.app.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Component("deepFeatureExtractor")
public class DeepFeatureExtractor {

    private static final Logger log = LoggerFactory.getLogger(DeepFeatureExtractor.class);

    private static volatile DeepFeatureExtractor INSTANCE;
    public static volatile boolean isAvailable = false;

    private volatile Process pythonProcess;
    private volatile BufferedWriter stdin;
    private volatile BufferedReader stdout;
    private volatile int featureDim = 1280;
    private volatile boolean loadFailed = false;

    @Value("${search.image.siamese-model-path:}")
    private String customModelPath;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @PostConstruct
    public void init() {
        INSTANCE = this;
        if (loadFailed) {
            log.warn("DeepFeatureExtractor previously failed, skipping init");
            return;
        }
        // Init in background thread to avoid blocking Spring Boot startup
        // when CUDA model loading fails or hangs
        new Thread(() -> {
            try {
                Path modelPath = findModelPath();
                if (modelPath == null) {
                    log.warn("MobileNetV2 model not found");
                    return;
                }
                Path scriptPath = extractScriptFromClasspath();
                if (scriptPath == null) {
                    log.warn("Python extract script not found in classpath");
                    return;
                }
                startPythonProcess(scriptPath.toString(), modelPath.toString());
                isAvailable = true;
                log.info("DeepFeatureExtractor initialized via Python GPU process. Feature dim: {}", featureDim);
            } catch (Exception e) {
                log.error("Failed to init DeepFeatureExtractor: {}", e.getMessage());
                loadFailed = true;
                isAvailable = false;
            }
        }, "deep-feat-init").start();
    }

    private Path findModelPath() {
        String[] searchPaths = {
                "d:/models/mobilenet_v2_feat/mobilenet_v2_feat.pt",
                customModelPath,
                "backend/models/mobilenet_v2_feat/mobilenet_v2_feat.pt",
                "models/mobilenet_v2_feat/mobilenet_v2_feat.pt",
                "../models/mobilenet_v2_feat/mobilenet_v2_feat.pt",
                "d:/客户端测试/backend/models/mobilenet_v2_feat/mobilenet_v2_feat.pt"
        };
        for (String sp : searchPaths) {
            if (sp == null || sp.isEmpty()) continue;
            try {
                Path p = Paths.get(sp);
                if (Files.exists(p)) {
                    return p.toAbsolutePath();
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private Path extractScriptFromClasspath() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("python/extract_features.py")) {
            if (is == null) {
                log.warn("extract_features.py not found in classpath resources");
                return null;
            }
            Path tempDir = Files.createTempDirectory("djl-feat-ext");
            tempDir.toFile().deleteOnExit();
            Path scriptFile = tempDir.resolve("extract_features.py");
            Files.copy(is, scriptFile);
            scriptFile.toFile().deleteOnExit();
            log.info("Extracted Python script to: {}", scriptFile);
            return scriptFile;
        } catch (IOException e) {
            log.error("Failed to extract Python script: {}", e.getMessage(), e);
            return null;
        }
    }

    private void startPythonProcess(String scriptPath, String modelPath) throws IOException {
        String pythonExe = findPython();
        log.info("Starting Python process: {} {} {}", pythonExe, scriptPath, modelPath);

        ProcessBuilder pb = new ProcessBuilder(
                pythonExe, scriptPath, modelPath
        );
        pb.directory(Paths.get(scriptPath).getParent().toFile());
        pb.redirectErrorStream(false);

        this.pythonProcess = pb.start();
        this.stdin = new BufferedWriter(new OutputStreamWriter(pythonProcess.getOutputStream(), StandardCharsets.UTF_8));
        this.stdout = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream(), StandardCharsets.UTF_8));

        // Start stderr reader thread (for debug logs from Python)
        Thread stderrReader = new Thread(() -> {
            try (BufferedReader errReader = new BufferedReader(
                    new InputStreamReader(pythonProcess.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = errReader.readLine()) != null) {
                    log.info("[python] {}", line);
                }
            } catch (IOException ignored) {}
        }, "python-stderr-reader");
        stderrReader.setDaemon(true);
        stderrReader.start();

        log.info("Python feature extractor process started (PID: {})", pythonProcess.pid());
    }

    private String findPython() {
        String[] candidates = {"python", "python3", "py"};
        for (String cmd : candidates) {
            try {
                ProcessBuilder pb = new ProcessBuilder(cmd, "--version");
                pb.redirectErrorStream(true);
                Process p = pb.start();
                String output = new String(p.getInputStream().readAllBytes()).trim();
                p.waitFor();
                if (p.exitValue() == 0 && output.contains("Python")) {
                    log.info("Found Python: {} -> {}", cmd, output);
                    return cmd;
                }
            } catch (Exception ignored) {}
        }
        return "python";
    }

    public float[] extract(BufferedImage sourceImage) {
        if (!isAvailable || pythonProcess == null || !pythonProcess.isAlive()) {
            throw new RuntimeException("DeepFeatureExtractor not available");
        }
        Path tempFile = null;
        try {
            // Save image to temp PNG file
            tempFile = Files.createTempFile("feat_", ".png");
            ImageIO.write(sourceImage, "png", tempFile.toFile());

            // Send path to Python process
            synchronized (this) {
                stdin.write(tempFile.toString());
                stdin.newLine();
                stdin.flush();

                // Read JSON result
                String jsonLine = stdout.readLine();
                if (jsonLine == null) {
                    throw new RuntimeException("Python process closed stdout unexpectedly");
                }

                // Read DONE marker
                String doneLine = stdout.readLine();

                @SuppressWarnings("unchecked")
                Map<String, Object> result = MAPPER.readValue(jsonLine, Map.class);

                if (result.containsKey("error")) {
                    throw new RuntimeException("Python extract error: " + result.get("error"));
                }

                @SuppressWarnings("unchecked")
                List<Number> featList = (List<Number>) result.get("features");
                float[] features = new float[featList.size()];
                for (int i = 0; i < featList.size(); i++) {
                    features[i] = featList.get(i).floatValue();
                }
                return features;
            }
        } catch (Exception e) {
            log.error("Feature extraction failed: {}", e.getMessage(), e);
            throw new RuntimeException("Feature extraction failed", e);
        } finally {
            if (tempFile != null) {
                try { Files.deleteIfExists(tempFile); } catch (IOException ignored) {}
            }
        }
    }

    public int getFeatureDim() {
        return featureDim;
    }

    // ---- Static convenience methods (same signatures as before) ----

    public static boolean isAvailable() {
        return isAvailable;
    }

    public static float[] extractSafe(BufferedImage sourceImage) {
        if (INSTANCE == null || !isAvailable) {
            log.warn("extractSafe: INSTANCE={}, isAvailable={}", INSTANCE != null, isAvailable);
            return null;
        }
        try {
            return INSTANCE.extract(sourceImage);
        } catch (Exception e) {
            log.warn("extractSafe failed: {}", e.getMessage(), e);
            return null;
        }
    }

    public static void reset() {
        if (INSTANCE != null) {
            INSTANCE.destroy();
        }
        isAvailable = false;
        INSTANCE = null;
    }

    public static double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) return 0;
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += (double) a[i] * b[i];
            na += (double) a[i] * a[i];
            nb += (double) b[i] * b[i];
        }
        if (na < 1e-12 || nb < 1e-12) return 0;
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    public static byte[] toBytes(float[] features) {
        byte[] bytes = new byte[features.length * 4];
        for (int i = 0; i < features.length; i++) {
            int bits = Float.floatToIntBits(features[i]);
            bytes[i * 4] = (byte) (bits >> 24);
            bytes[i * 4 + 1] = (byte) (bits >> 16);
            bytes[i * 4 + 2] = (byte) (bits >> 8);
            bytes[i * 4 + 3] = (byte) (bits);
        }
        return bytes;
    }

    public static float[] fromBytes(byte[] bytes) {
        float[] features = new float[bytes.length / 4];
        for (int i = 0; i < features.length; i++) {
            int bits = ((bytes[i * 4] & 0xFF) << 24)
                    | ((bytes[i * 4 + 1] & 0xFF) << 16)
                    | ((bytes[i * 4 + 2] & 0xFF) << 8)
                    | (bytes[i * 4 + 3] & 0xFF);
            features[i] = Float.intBitsToFloat(bits);
        }
        return features;
    }

    @PreDestroy
    public void destroy() {
        isAvailable = false;
        if (pythonProcess != null && pythonProcess.isAlive()) {
            try { stdin.close(); } catch (Exception ignored) {}
            try { stdout.close(); } catch (Exception ignored) {}
            pythonProcess.destroy();
            try { pythonProcess.waitFor(5, java.util.concurrent.TimeUnit.SECONDS); } catch (Exception e) {
                pythonProcess.destroyForcibly();
            }
            log.info("Python feature extractor process stopped");
        }
        pythonProcess = null;
        stdin = null;
        stdout = null;
    }
}
