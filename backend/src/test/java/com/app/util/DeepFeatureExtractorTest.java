package com.app.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DeepFeatureExtractor MobileNetV2 integration.
 * Tests require the MobileNetV2 model file at backend/models/mobilenet_v2_feat/mobilenet_v2_feat.pt
 */
class DeepFeatureExtractorTest {

    private static boolean modelAvailable = false;

    @BeforeAll
    static void checkModel() {
        java.nio.file.Path modelPath = java.nio.file.Paths.get("d:/models/mobilenet_v2_feat/mobilenet_v2_feat.pt");
        if (!java.nio.file.Files.exists(modelPath)) {
            modelPath = java.nio.file.Paths.get("backend/models/mobilenet_v2_feat/mobilenet_v2_feat.pt");
        }
        modelAvailable = java.nio.file.Files.exists(modelPath);
    }

    static boolean modelAvailable() {
        return modelAvailable;
    }

    private BufferedImage createTestImage(int width, int height, int rgb) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new java.awt.Color(rgb));
        g.fillRect(0, 0, width, height);
        g.dispose();
        return img;
    }

    private BufferedImage createGradientImage(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = (int) (255.0 * x / width);
                int g = (int) (255.0 * y / height);
                int b = 128;
                img.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return img;
    }

    @Test
    @EnabledIf("modelAvailable")
    void testExtractReturnsCorrectDimension() {
        DeepFeatureExtractor extractor = new DeepFeatureExtractor();
        extractor.init();

        assertTrue(DeepFeatureExtractor.isAvailable(), "MobileNetV2 model should be available");

        BufferedImage img = createGradientImage(320, 240);
        float[] features = extractor.extract(img);

        assertNotNull(features, "Features should not be null");
        assertEquals(1280, features.length, "MobileNetV2 should produce 1280-dim features");

        extractor.destroy();
    }

    @Test
    @EnabledIf("modelAvailable")
    void testSameImageProducesSameFeatures() {
        DeepFeatureExtractor extractor = new DeepFeatureExtractor();
        extractor.init();

        BufferedImage img = createGradientImage(320, 240);
        float[] features1 = extractor.extract(img);
        float[] features2 = extractor.extract(img);

        assertArrayEquals(features1, features2, "Same image should produce identical features");

        extractor.destroy();
    }

    @Test
    @EnabledIf("modelAvailable")
    void testCosineSimilarityIdenticalImages() {
        DeepFeatureExtractor extractor = new DeepFeatureExtractor();
        extractor.init();

        BufferedImage img = createGradientImage(320, 240);
        float[] features = extractor.extract(img);

        double sim = DeepFeatureExtractor.cosineSimilarity(features, features);
        assertEquals(1.0, sim, 0.001, "Identical features should have cosine similarity 1.0");

        extractor.destroy();
    }

    @Test
    @EnabledIf("modelAvailable")
    void testCosineSimilarityDifferentImages() {
        DeepFeatureExtractor extractor = new DeepFeatureExtractor();
        extractor.init();

        BufferedImage img1 = createTestImage(320, 240, 0xFF0000); // Red
        BufferedImage img2 = createTestImage(320, 240, 0x0000FF); // Blue

        float[] features1 = extractor.extract(img1);
        float[] features2 = extractor.extract(img2);

        double sim = DeepFeatureExtractor.cosineSimilarity(features1, features2);
        assertTrue(sim < 0.95, "Very different images should have low similarity, got: " + sim);
        assertTrue(sim > -1.0, "Similarity should be >= -1.0");

        extractor.destroy();
    }

    @Test
    @EnabledIf("modelAvailable")
    void testDifferentSizeImages() {
        DeepFeatureExtractor extractor = new DeepFeatureExtractor();
        extractor.init();

        BufferedImage img1 = createGradientImage(320, 240);
        BufferedImage img2 = createGradientImage(640, 480);
        BufferedImage img3 = createGradientImage(224, 224);

        float[] features1 = extractor.extract(img1);
        float[] features2 = extractor.extract(img2);
        float[] features3 = extractor.extract(img3);

        double sim12 = DeepFeatureExtractor.cosineSimilarity(features1, features2);
        double sim13 = DeepFeatureExtractor.cosineSimilarity(features1, features3);
        assertTrue(sim12 > 0.8, "Same content at different sizes should be similar, got: " + sim12);
        assertTrue(sim13 > 0.8, "Same content at different sizes should be similar, got: " + sim13);

        extractor.destroy();
    }

    @Test
    void testCosineSimilarityStaticMethod() {
        float[] a = {1.0f, 0.0f, 0.0f};
        float[] b = {0.0f, 1.0f, 0.0f};
        float[] c = {1.0f, 0.0f, 0.0f};

        assertEquals(0.0, DeepFeatureExtractor.cosineSimilarity(a, b), 0.001, "Orthogonal vectors");
        assertEquals(1.0, DeepFeatureExtractor.cosineSimilarity(a, c), 0.001, "Identical vectors");
    }

    @Test
    void testCosineSimilarityEdgeCases() {
        assertEquals(0.0, DeepFeatureExtractor.cosineSimilarity(null, new float[]{1.0f}), "Null input");
        assertEquals(0.0, DeepFeatureExtractor.cosineSimilarity(new float[]{1.0f}, new float[]{1.0f, 2.0f}), "Different lengths");
    }

    @Test
    void testToBytesAndFromBytes() {
        float[] original = {0.12345f, -0.6789f, 1.0f, 0.0f, -1.0f};
        byte[] bytes = DeepFeatureExtractor.toBytes(original);
        float[] restored = DeepFeatureExtractor.fromBytes(bytes);

        assertEquals(original.length, restored.length, "Length should match");
        for (int i = 0; i < original.length; i++) {
            assertEquals(original[i], restored[i], 0.0001f, "Value at index " + i + " should match");
        }
    }

    @Test
    void testGetFeatureDim() {
        DeepFeatureExtractor extractor = new DeepFeatureExtractor();
        assertEquals(1280, extractor.getFeatureDim(), "Default feature dim should be 1280");
    }
}
