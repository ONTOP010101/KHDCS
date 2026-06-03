package com.app.util;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.Normalize;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.Pipeline;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component("deepFeatureExtractor")
public class DeepFeatureExtractor {

    private static final Logger log = LoggerFactory.getLogger(DeepFeatureExtractor.class);

    private static volatile DeepFeatureExtractor INSTANCE;

    public static volatile boolean isAvailable = false;

    private volatile ZooModel<Image, float[]> model;
    private volatile Predictor<Image, float[]> predictor;
    private volatile boolean loadFailed = false;
    private volatile int featureDim = 1280;

    @Value("${search.image.siamese-model-path:}")
    private String customModelPath;

    // MobileNetV2 ImageNet normalization
    private static final float[] MOBILENET_MEAN = {0.485f, 0.456f, 0.406f};
    private static final float[] MOBILENET_STD = {0.229f, 0.224f, 0.225f};

    @PostConstruct
    public void init() {
        INSTANCE = this;
        if (loadFailed) {
            log.warn("DeepFeatureExtractor previously failed to load, skipping init");
            return;
        }
        try {
            loadMobileNetModel();
            if (!isAvailable) {
                log.warn("MobileNetV2 model failed, system will run without deep features");
            }
        } catch (Exception e) {
            log.error("Failed to initialize DeepFeatureExtractor: {}", e.getMessage(), e);
            loadFailed = true;
            isAvailable = false;
        }
    }

    private void loadMobileNetModel() {
        try {
            Path modelPath = findModelPath();
            if (modelPath == null) {
                log.warn("MobileNetV2 model not found in any search path");
                return;
            }
            log.info("Loading MobileNetV2 from: {}", modelPath);

            Criteria<Image, float[]> criteria = Criteria.builder()
                    .setTypes(Image.class, float[].class)
                    .optEngine("PyTorch")
                    .optModelPath(modelPath)
                    .optTranslator(new MobileNetTranslator())
                    .optOption("mapLocation", "true")
                    .build();

            this.model = criteria.loadModel();
            this.predictor = model.newPredictor(new MobileNetTranslator());
            this.featureDim = 1280;
            isAvailable = true;
            log.info("MobileNetV2 loaded successfully. Feature dimension: {}", featureDim);
        } catch (Exception e) {
            log.error("Failed to load MobileNetV2 model: {}", e.getMessage(), e);
            isAvailable = false;
        }
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
            if (sp == null) continue;
            try {
                Path p = Paths.get(sp);
                if (Files.exists(p)) {
                    return p.toAbsolutePath();
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    public float[] extract(BufferedImage sourceImage) {
        if (!isAvailable || predictor == null) {
            throw new RuntimeException("DeepFeatureExtractor not available");
        }
        try {
            Image img = ImageFactory.getInstance().fromImage(sourceImage);
            float[] result = predictor.predict(img);
            log.debug("MobileNetV2 extract: dim={}, first5=[{},{},{},{},{}]",
                    result.length,
                    result.length > 0 ? result[0] : 0,
                    result.length > 1 ? result[1] : 0,
                    result.length > 2 ? result[2] : 0,
                    result.length > 3 ? result[3] : 0,
                    result.length > 4 ? result[4] : 0);
            return result;
        } catch (Exception e) {
            log.error("Feature extraction failed: {}", e.getMessage(), e);
            throw new RuntimeException("Feature extraction failed", e);
        }
    }

    public int getFeatureDim() {
        return featureDim;
    }

    // ---- Static convenience methods ----

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
        if (predictor != null) { try { predictor.close(); } catch (Exception e) {} predictor = null; }
        if (model != null) { try { model.close(); } catch (Exception e) {} model = null; }
        isAvailable = false;
    }

    /**
     * MobileNetV2 Translator with standard ImageNet preprocessing.
     * Pipeline: BufferedImage -> NDArray -> Resize 224x224 -> ToTensor /255 -> Normalize
     */
    public static class MobileNetTranslator implements Translator<Image, float[]> {

        @Override
        public NDList processInput(TranslatorContext ctx, Image input) {
            NDManager manager = ctx.getNDManager();
            NDArray arr = input.toNDArray(manager);

            // Drop alpha channel if present (RGBA -> RGB)
            if (arr.getShape().size() == 4 && arr.getShape().get(2) == 4) {
                arr = arr.get(":,:,:,0:3");
            }

            // Handle unexpected batch dimension from toNDArray()
            if (arr.getShape().size() == 4 && arr.getShape().get(0) == 1) {
                arr = arr.squeeze(0);
            }

            Pipeline pipeline = new Pipeline();
            pipeline.add(new Resize(224, 224));
            pipeline.add(new ToTensor());
            pipeline.add(new Normalize(MOBILENET_MEAN, MOBILENET_STD));

            NDList transformed = pipeline.transform(new NDList(arr));
            NDArray result = transformed.get(0);

            // Ensure [1, C, H, W] for model input
            if (result.getShape().size() == 3) {
                result = result.expandDims(0);
            } else if (result.getShape().size() == 5) {
                result = result.squeeze(0).squeeze(0).expandDims(0);
            }

            return new NDList(result);
        }

        @Override
        public float[] processOutput(TranslatorContext ctx, NDList list) {
            NDArray output = list.get(0);
            if (output.getShape().size() > 1) {
                output = output.squeeze(0);
            }
            float[] result = output.toFloatArray();
            output.close();
            return result;
        }
    }
}
