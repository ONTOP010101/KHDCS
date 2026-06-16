package com.app.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

public class ImageHashUtil {

    public static long computeDHash(byte[] imageBytes) throws IOException {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (img == null) throw new IOException("无法解析图片");
        return computeDHash(preprocess(img));
    }

    public static long computeDHash(Path filePath) throws IOException {
        BufferedImage img = ImageIO.read(filePath.toFile());
        if (img == null) throw new IOException("无法解析图片: " + filePath);
        return computeDHash(preprocess(img));
    }

    public static long computeDHash(InputStream inputStream) throws IOException {
        BufferedImage img = ImageIO.read(inputStream);
        if (img == null) throw new IOException("无法解析图片");
        return computeDHash(preprocess(img));
    }

    public static long computePHash(byte[] imageBytes) throws IOException {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (img == null) throw new IOException("无法解析图片");
        return computePHash(preprocess(img));
    }

    public static long computePHash(Path filePath) throws IOException {
        BufferedImage img = ImageIO.read(filePath.toFile());
        if (img == null) throw new IOException("无法解析图片: " + filePath);
        return computePHash(preprocess(img));
    }

    public static long computePHashFromBytes(byte[] imageBytes) {
        try {
            return computePHash(imageBytes);
        } catch (Exception e) {
            return 0;
        }
    }

    public static long computePHashFromImage(BufferedImage image) {
        try {
            return computePHash(preprocess(image));
        } catch (Exception e) {
            return 0;
        }
    }

    public static long computePHashFromFile(Path filePath) {
        try {
            return computePHash(filePath);
        } catch (Exception e) {
            return 0;
        }
    }

    public static long computeDHashFromBytes(byte[] imageBytes) {
        try {
            return computeDHash(imageBytes);
        } catch (Exception e) {
            return 0;
        }
    }

    public static long computeDHashFromImage(BufferedImage image) {
        try {
            return computeDHash(preprocess(image));
        } catch (Exception e) {
            return 0;
        }
    }

    public static long computeDHashFromFile(Path filePath) {
        try {
            return computeDHash(filePath);
        } catch (Exception e) {
            return 0;
        }
    }

    public static long computeDHashCropped(byte[] imageBytes) throws IOException {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (img == null) throw new IOException("无法解析图片");
        return computeDHash(preprocess(cropCenter(img)));
    }

    public static long computeDHashCroppedFromBytes(byte[] imageBytes) {
        try {
            return computeDHashCropped(imageBytes);
        } catch (Exception e) {
            return 0;
        }
    }

    public static boolean isScreenshotLike(byte[] imageBytes) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (img == null) return false;
            return isScreenshotLike(img);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isScreenshotLike(BufferedImage img) {
        if (img == null) return false;
        int w = img.getWidth();
        int h = img.getHeight();
        if (w <= 0 || h <= 0) return false;
        double ratio = (double) h / w;
        return ratio > 1.4 && h > 1000;
    }

    public static List<Long> computeMultiCropDHashes(byte[] imageBytes) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (img == null) return new ArrayList<>();
            return computeMultiCropDHashes(img);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static List<Long> computeMultiCropDHashes(BufferedImage img) {
        List<Long> hashes = new ArrayList<>();
        int w = img.getWidth();
        int h = img.getHeight();
        if (w <= 0 || h <= 0) return hashes;

        int imgW = w;
        int imgH = h;
        int gridCols = 3;
        int gridRows = 4;
        int cellW = imgW / gridCols;
        int cellH = imgH / gridRows;
        double stepX = cellW * 0.5;
        double stepY = cellH * 0.5;

        for (double startY = 0; startY + cellH <= imgH; startY += stepY) {
            int sy = Math.max(0, (int) startY);
            for (double startX = 0; startX + cellW <= imgW; startX += stepX) {
                int sx = Math.max(0, (int) startX);
                int cw = Math.min(cellW, imgW - sx);
                int ch = Math.min(cellH, imgH - sy);
                if (cw < 40 || ch < 40) continue;
                BufferedImage sub = img.getSubimage(sx, sy, cw, ch);
                long dhash = computeDHashFromImage(sub);
                if (dhash != 0) hashes.add(dhash);
            }
        }
        return hashes;
    }

    public static List<Long> computeMultiCropPHashes(byte[] imageBytes) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (img == null) return new ArrayList<>();
            return computeMultiCropPHashes(img);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static List<Long> computeMultiCropPHashes(BufferedImage img) {
        List<Long> hashes = new ArrayList<>();
        int w = img.getWidth();
        int h = img.getHeight();
        if (w <= 0 || h <= 0) return hashes;

        int imgW = w;
        int imgH = h;
        int gridCols = 3;
        int gridRows = 4;
        int cellW = imgW / gridCols;
        int cellH = imgH / gridRows;
        double stepX = cellW * 0.5;
        double stepY = cellH * 0.5;

        for (double startY = 0; startY + cellH <= imgH; startY += stepY) {
            int sy = Math.max(0, (int) startY);
            for (double startX = 0; startX + cellW <= imgW; startX += stepX) {
                int sx = Math.max(0, (int) startX);
                int cw = Math.min(cellW, imgW - sx);
                int ch = Math.min(cellH, imgH - sy);
                if (cw < 40 || ch < 40) continue;
                BufferedImage sub = img.getSubimage(sx, sy, cw, ch);
                long phash = computePHashFromImage(sub);
                if (phash != 0) hashes.add(phash);
            }
        }
        return hashes;
    }

    public static int hammingDistance(long hash1, long hash2) {
        return Long.bitCount(hash1 ^ hash2);
    }

    public static double combinedScore(int dDist, int pDist) {
        return dDist * 0.5 + pDist * 0.5;
    }

    public static double combinedScore(int dDist, int pDist, double dWeight, double pWeight) {
        return dDist * dWeight + pDist * pWeight;
    }

    public static int[] computeBuckets(long dhash) {
        return new int[] {
            (int) ((dhash >> 48) & 0xFFFF),
            (int) ((dhash >> 32) & 0xFFFF),
            (int) ((dhash >> 16) & 0xFFFF),
            (int) (dhash & 0xFFFF)
        };
    }

    public static String buildBucketWhereClause(int[] buckets) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append("(dh_bucket0 = ").append(buckets[0]).append(" AND dh_bucket1 = ").append(buckets[1]).append(")");
        sb.append(" OR (dh_bucket0 = ").append(buckets[0]).append(" AND dh_bucket2 = ").append(buckets[2]).append(")");
        sb.append(" OR (dh_bucket0 = ").append(buckets[0]).append(" AND dh_bucket3 = ").append(buckets[3]).append(")");
        sb.append(" OR (dh_bucket1 = ").append(buckets[1]).append(" AND dh_bucket2 = ").append(buckets[2]).append(")");
        sb.append(" OR (dh_bucket1 = ").append(buckets[1]).append(" AND dh_bucket3 = ").append(buckets[3]).append(")");
        sb.append(" OR (dh_bucket2 = ").append(buckets[2]).append(" AND dh_bucket3 = ").append(buckets[3]).append(")");
        sb.append(")");
        return sb.toString();
    }

    private static BufferedImage preprocess(BufferedImage source) {
        int w = source.getWidth();
        int h = source.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(source, 0, 0, w, h, null);
        g.dispose();

        int[] pixels = new int[w * h];
        result.getRGB(0, 0, w, h, pixels, 0, w);

        int minGray = 255, maxGray = 0;
        for (int px : pixels) {
            int gray = rgbToGray(px);
            if (gray < minGray) minGray = gray;
            if (gray > maxGray) maxGray = gray;
        }

        if (maxGray > minGray + 20) {
            float scale = 255.0f / (maxGray - minGray);
            int[] newPixels = new int[pixels.length];
            for (int i = 0; i < pixels.length; i++) {
                int a = (pixels[i] >> 24) & 0xFF;
                int r = (pixels[i] >> 16) & 0xFF;
                int gv = (pixels[i] >> 8) & 0xFF;
                int b = pixels[i] & 0xFF;
                int gray = rgbToGray(pixels[i]);
                float factor = (gray - minGray) * scale / 255.0f;
                factor = Math.max(0, Math.min(1, factor));
                int nr = (int) (r * (0.7 + 0.3 * factor));
                int ng = (int) (gv * (0.7 + 0.3 * factor));
                int nb = (int) (b * (0.7 + 0.3 * factor));
                nr = Math.min(255, Math.max(0, nr));
                ng = Math.min(255, Math.max(0, ng));
                nb = Math.min(255, Math.max(0, nb));
                newPixels[i] = (a << 24) | (nr << 16) | (ng << 8) | nb;
            }
            result.setRGB(0, 0, w, h, newPixels, 0, w);
        }

        return result;
    }

    private static long computeDHash(BufferedImage source) {
        BufferedImage scaled = new BufferedImage(9, 8, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(source, 0, 0, 9, 8, null);
        g.dispose();

        long hash = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int left = rgbToGray(scaled.getRGB(x, y));
                int right = rgbToGray(scaled.getRGB(x + 1, y));
                if (left > right) {
                    hash |= (1L << (y * 8 + x));
                }
            }
        }
        return hash;
    }

    private static long computePHash(BufferedImage source) {
        int size = 32;
        BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(source, 0, 0, size, size, null);
        g.dispose();

        double[][] matrix = new double[size][size];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                matrix[y][x] = rgbToGray(scaled.getRGB(x, y));
            }
        }

        dct2D(matrix, size);

        double[] lowFreq = new double[64];
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                lowFreq[y * 8 + x] = matrix[y][x];
            }
        }

        double[] sorted = lowFreq.clone();
        Arrays.sort(sorted);
        double median = sorted[32];

        long hash = 0;
        for (int i = 0; i < 64; i++) {
            if (lowFreq[i] > median) {
                hash |= (1L << i);
            }
        }
        return hash;
    }

    private static void dct2D(double[][] matrix, int size) {
        for (int i = 0; i < size; i++) {
            double[] row = new double[size];
            for (int j = 0; j < size; j++) row[j] = matrix[i][j];
            row = dct1D(row, size);
            for (int j = 0; j < size; j++) matrix[i][j] = row[j];
        }
        for (int j = 0; j < size; j++) {
            double[] col = new double[size];
            for (int i = 0; i < size; i++) col[i] = matrix[i][j];
            col = dct1D(col, size);
            for (int i = 0; i < size; i++) matrix[i][j] = col[i];
        }
    }

    private static double[] dct1D(double[] input, int size) {
        double[] output = new double[size];
        double factor = Math.PI / size;
        for (int k = 0; k < size; k++) {
            double sum = 0;
            for (int n = 0; n < size; n++) {
                sum += input[n] * Math.cos((n + 0.5) * k * factor);
            }
            output[k] = sum;
        }
        return output;
    }

    private static BufferedImage cropCenter(BufferedImage source) {
        int w = source.getWidth();
        int h = source.getHeight();
        int cropTop = (int) (h * 0.12);
        int cropBottom = (int) (h * 0.12);
        int newH = h - cropTop - cropBottom;
        if (newH <= 0) return source;
        return source.getSubimage(0, cropTop, w, newH);
    }

    private static int rgbToGray(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (r * 30 + g * 59 + b * 11) / 100;
    }
}
