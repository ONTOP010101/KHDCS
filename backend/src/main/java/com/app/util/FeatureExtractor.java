package com.app.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;

public class FeatureExtractor {

    private static final int TARGET_SIZE = 128;
    private static final int GRID = 8;
    private static final int CELL = TARGET_SIZE / GRID;
    private static final int EDGE_BINS = 8;
    private static final int L_BINS = 4;
    private static final int A_BINS = 3;
    private static final int B_BINS = 3;
    private static final int LBP_BINS = 10;
    private static final int MAG_BINS = 1;

    private static final float EDGE_WEIGHT = 1.0f;
    private static final float COLOR_WEIGHT = 1.733f;
    private static final float TEXTURE_WEIGHT = 1.0f;
    private static final float MOMENTS_WEIGHT = 0.5f;
    private static final float MAG_WEIGHT = 0.7f;
    private static final ColorConvertOp SRGB_CONVERTER = new ColorConvertOp(
            ColorSpace.getInstance(ColorSpace.CS_sRGB), null);

    public static float[] extract(BufferedImage source) {
        BufferedImage img = resize(toSrgb(source), TARGET_SIZE, TARGET_SIZE);

        int w = img.getWidth();
        int h = img.getHeight();
        int[] pixels = img.getRGB(0, 0, w, h, null, 0, w);

        float[] e8 = extractEdgeHistogram(pixels, w, h);
        float[] c8 = extractColorHistogram(pixels, w, h);
        float[] t8 = extractTexture(pixels, w, h);
        float[] m8 = extractEdgeMagnitude(pixels, w, h);

        float[] e4 = poolFeatures(e8, EDGE_BINS, 2);
        float[] e2 = poolFeatures(e4, EDGE_BINS, 2);
        float[] e1 = poolFeatures(e2, EDGE_BINS, 2);

        int cBins = L_BINS * A_BINS * B_BINS;
        float[] c4 = poolFeatures(c8, cBins, 2);
        float[] c2 = poolFeatures(c4, cBins, 2);
        float[] c1 = poolFeatures(c2, cBins, 2);

        float[] t4 = poolFeatures(t8, LBP_BINS, 2);
        float[] t2 = poolFeatures(t4, LBP_BINS, 2);
        float[] t1 = poolFeatures(t2, LBP_BINS, 2);

        float[] m4 = poolFeatures(m8, MAG_BINS, 2);
        float[] m2 = poolFeatures(m4, MAG_BINS, 2);
        float[] m1 = poolFeatures(m2, MAG_BINS, 2);

        float[] moments = extractGlobalMoments(pixels, w, h);

        float[] edgeVec = concat(new float[][]{e1, e2, e4, e8});
        float[] colorVec = concat(new float[][]{c1, c2, c4, c8});
        float[] textureVec = concat(new float[][]{t1, t2, t4, t8});
        float[] magVec = concat(new float[][]{m1, m2, m4, m8});

        l2Normalize(edgeVec);
        l2Normalize(colorVec);
        l2Normalize(textureVec);
        l2Normalize(moments);
        l2Normalize(magVec);

        return concat(new float[][]{
                scaleVec(edgeVec, EDGE_WEIGHT),
                scaleVec(colorVec, COLOR_WEIGHT),
                scaleVec(textureVec, TEXTURE_WEIGHT),
                scaleVec(moments, MOMENTS_WEIGHT),
                scaleVec(magVec, MAG_WEIGHT)
        });
    }

    public static double cosineSimilarity(float[] a, float[] b) {
        if (a.length == 0 || b.length == 0) return 0;
        double dot = 0, normA = 0, normB = 0;
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA < 1e-10 || normB < 1e-10) return 0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public static BufferedImage autoCropScreenshot(BufferedImage source) {
        source = toSrgb(source);
        int w = source.getWidth();
        int h = source.getHeight();

        int[] pixels = new int[w * h];
        source.getRGB(0, 0, w, h, pixels, 0, w);

        int sampleStep = Math.max(1, h / 200);
        double[] variances = new double[h];
        for (int y = 0; y < h; y += sampleStep) {
            double sum = 0, sumSq = 0;
            int count = 0;
            for (int x = 0; x < w; x++) {
                int rgb = pixels[y * w + x];
                int gray = ((rgb >> 16) & 0xFF) * 30 + ((rgb >> 8) & 0xFF) * 59 + (rgb & 0xFF) * 11;
                gray /= 100;
                sum += gray;
                sumSq += gray * gray;
                count++;
            }
            double mean = sum / count;
            double var = sumSq / count - mean * mean;
            for (int dy = 0; dy < sampleStep && y + dy < h; dy++) {
                variances[y + dy] = var;
            }
        }

        double[] smoothed = new double[h];
        int smoothRadius = h / 40;
        for (int y = smoothRadius; y < h - smoothRadius; y++) {
            double s = 0;
            for (int dy = -smoothRadius; dy <= smoothRadius; dy++) s += variances[y + dy];
            smoothed[y] = s / (2 * smoothRadius + 1);
        }

        double totalVar = 0;
        for (int y = smoothRadius; y < h - smoothRadius; y++) totalVar += smoothed[y];
        double meanVar = totalVar / (h - 2 * smoothRadius);

        double threshold = meanVar * 1.6;

        int bestStart = h / 4, bestEnd = h / 2, bestWidth = 0;
        int start = -1;
        for (int y = h / 6; y < h * 5 / 6; y++) {
            if (smoothed[y] > threshold) {
                if (start < 0) start = y;
            } else if (start >= 0) {
                int width = y - start;
                if (width > bestWidth && width > h / 10) {
                    bestStart = start;
                    bestEnd = y;
                    bestWidth = width;
                }
                start = -1;
            }
        }
        if (start >= 0 && (h * 5 / 6 - start) > bestWidth) {
            bestStart = start;
            bestEnd = h * 5 / 6;
        }

        if (bestWidth < h / 10) return source;

        int margin = bestWidth / 10;
        int cropY = Math.max(0, bestStart - margin);
        int cropH = Math.min(h - cropY, (bestEnd - bestStart) + margin * 2);

        double[] colVars = new double[w];
        for (int x = 0; x < w; x++) {
            double sum = 0, sumSq = 0;
            int count = 0;
            for (int y = cropY; y < cropY + cropH; y++) {
                int rgb = pixels[y * w + x];
                int gray = ((rgb >> 16) & 0xFF) * 30 + ((rgb >> 8) & 0xFF) * 59 + (rgb & 0xFF) * 11;
                gray /= 100;
                sum += gray;
                sumSq += gray * gray;
                count++;
            }
            double mean = sum / count;
            colVars[x] = sumSq / count - mean * mean;
        }

        double colMean = 0;
        for (double v : colVars) colMean += v;
        colMean /= w;
        double colThresh = colMean * 1.2;

        int left = 0, right = w;
        for (int x = 0; x < w / 2; x++) {
            if (colVars[x] > colThresh) { left = Math.max(0, x - 8); break; }
        }
        for (int x = w - 1; x > w / 2; x--) {
            if (colVars[x] > colThresh) { right = Math.min(w, x + 8); break; }
        }

        if (right - left > w / 3 && cropH > h / 10) {
            int cropW = right - left;
            BufferedImage cropped = new BufferedImage(cropW, cropH, BufferedImage.TYPE_INT_RGB);
            int[] cropPixels = source.getRGB(left, cropY, cropW, cropH, null, 0, cropW);
            cropped.setRGB(0, 0, cropW, cropH, cropPixels, 0, cropW);
            return cropped;
        }
        return source;
    }

    public static byte[] toBytes(float[] features) {
        byte[] bytes = new byte[features.length * 4];
        int off = 0;
        for (float f : features) {
            int bits = Float.floatToIntBits(f);
            bytes[off++] = (byte) (bits >> 24);
            bytes[off++] = (byte) (bits >> 16);
            bytes[off++] = (byte) (bits >> 8);
            bytes[off++] = (byte) bits;
        }
        return bytes;
    }

    public static float[] fromBytes(byte[] bytes) {
        float[] features = new float[bytes.length / 4];
        int off = 0;
        for (int i = 0; i < features.length; i++) {
            int bits = ((bytes[off++] & 0xFF) << 24)
                     | ((bytes[off++] & 0xFF) << 16)
                     | ((bytes[off++] & 0xFF) << 8)
                     | (bytes[off++] & 0xFF);
            features[i] = Float.intBitsToFloat(bits);
        }
        return features;
    }

    private static float[] extractEdgeHistogram(int[] pixels, int w, int h) {
        int binCount = GRID * GRID * EDGE_BINS;
        float[] hist = new float[binCount];

        for (int gy = 0; gy < GRID; gy++) {
            for (int gx = 0; gx < GRID; gx++) {
                float[] localBins = new float[EDGE_BINS];
                double totalMag = 0;

                for (int y = 1; y < CELL - 1; y++) {
                    for (int x = 1; x < CELL - 1; x++) {
                        int px = gx * CELL + x;
                        int py = gy * CELL + y;

                        int tl = gray(pixels[(py - 1) * w + (px - 1)]);
                        int t  = gray(pixels[(py - 1) * w + px]);
                        int tr = gray(pixels[(py - 1) * w + (px + 1)]);
                        int l  = gray(pixels[py * w + (px - 1)]);
                        int r  = gray(pixels[py * w + (px + 1)]);
                        int bl = gray(pixels[(py + 1) * w + (px - 1)]);
                        int b  = gray(pixels[(py + 1) * w + px]);
                        int br = gray(pixels[(py + 1) * w + (px + 1)]);

                        int gxVal = (tr + 2 * r + br) - (tl + 2 * l + bl);
                        int gyVal = (tl + 2 * t + tr) - (bl + 2 * b + br);

                        double mag = Math.sqrt(gxVal * gxVal + gyVal * gyVal);
                        double angle = Math.atan2(gyVal, gxVal);

                        if (angle < 0) angle += 2 * Math.PI;

                        int bin = (int) (angle / (2 * Math.PI) * EDGE_BINS);
                        if (bin >= EDGE_BINS) bin = EDGE_BINS - 1;
                        localBins[bin] += mag;
                        totalMag += mag;
                    }
                }

                double magThreshold = totalMag / ((CELL - 2) * (CELL - 2)) * 0.3;
                double localNorm = 0;
                for (int i = 0; i < EDGE_BINS; i++) {
                    if (localBins[i] < magThreshold) localBins[i] = 0;
                    localNorm += localBins[i] * localBins[i];
                }
                if (localNorm > 1e-10) {
                    localNorm = Math.sqrt(localNorm);
                    for (int i = 0; i < EDGE_BINS; i++) {
                        hist[(gy * GRID + gx) * EDGE_BINS + i] = localBins[i] / (float) localNorm;
                    }
                }
            }
        }
        return hist;
    }

    private static float[] extractColorHistogram(int[] pixels, int w, int h) {
        int binCount = GRID * GRID * L_BINS * A_BINS * B_BINS;
        float[] hist = new float[binCount];

        for (int gy = 0; gy < GRID; gy++) {
            for (int gx = 0; gx < GRID; gx++) {
                int[][][] labJoint = new int[L_BINS][A_BINS][B_BINS];
                int count = 0;

                for (int y = 0; y < CELL; y++) {
                    for (int x = 0; x < CELL; x++) {
                        int px = gx * CELL + x;
                        int py = gy * CELL + y;
                        float[] lab = rgbToLab(pixels[py * w + px]);

                        int lBin = (int) ((lab[0] / 100f) * L_BINS);
                        if (lBin >= L_BINS) lBin = L_BINS - 1;
                        if (lBin < 0) lBin = 0;

                        float aNorm = (lab[1] + 128f) / 256f;
                        int aBin = (int) (aNorm * A_BINS);
                        if (aBin >= A_BINS) aBin = A_BINS - 1;
                        if (aBin < 0) aBin = 0;

                        float bNorm = (lab[2] + 128f) / 256f;
                        int bBin = (int) (bNorm * B_BINS);
                        if (bBin >= B_BINS) bBin = B_BINS - 1;
                        if (bBin < 0) bBin = 0;

                        labJoint[lBin][aBin][bBin]++;
                        count++;
                    }
                }

                int base = (gy * GRID + gx) * L_BINS * A_BINS * B_BINS;
                double cellNorm = 0;
                for (int li = 0; li < L_BINS; li++) {
                    for (int ai = 0; ai < A_BINS; ai++) {
                        for (int bi = 0; bi < B_BINS; bi++) {
                            float val = count > 0 ? (float) labJoint[li][ai][bi] / count : 0;
                            int idx = base + (li * A_BINS + ai) * B_BINS + bi;
                            hist[idx] = val;
                            cellNorm += val * val;
                        }
                    }
                }
                if (cellNorm > 1e-10) {
                    cellNorm = Math.sqrt(cellNorm);
                    for (int li = 0; li < L_BINS; li++) {
                        for (int ai = 0; ai < A_BINS; ai++) {
                            for (int bi = 0; bi < B_BINS; bi++) {
                                int idx = base + (li * A_BINS + ai) * B_BINS + bi;
                                hist[idx] /= cellNorm;
                            }
                        }
                    }
                }
            }
        }
        return hist;
    }

    private static float[] extractTexture(int[] pixels, int w, int h) {
        int binCount = GRID * GRID * LBP_BINS;
        float[] hist = new float[binCount];

        for (int gy = 0; gy < GRID; gy++) {
            for (int gx = 0; gx < GRID; gx++) {
                int[] lbpHist = new int[LBP_BINS];
                int count = 0;

                for (int y = 1; y < CELL - 1; y++) {
                    for (int x = 1; x < CELL - 1; x++) {
                        int px = gx * CELL + x;
                        int py = gy * CELL + y;
                        int center = gray(pixels[py * w + px]);

                        int[] ng = new int[8];
                        ng[0] = gray(pixels[(py - 1) * w + px]);
                        ng[1] = gray(pixels[(py - 1) * w + (px + 1)]);
                        ng[2] = gray(pixels[py * w + (px + 1)]);
                        ng[3] = gray(pixels[(py + 1) * w + (px + 1)]);
                        ng[4] = gray(pixels[(py + 1) * w + px]);
                        ng[5] = gray(pixels[(py + 1) * w + (px - 1)]);
                        ng[6] = gray(pixels[py * w + (px - 1)]);
                        ng[7] = gray(pixels[(py - 1) * w + (px - 1)]);

                        int lbp = 0;
                        for (int k = 0; k < 8; k++) {
                            if (ng[k] > center) lbp |= (1 << k);
                        }

                        int transitions = 0;
                        for (int k = 0; k < 8; k++) {
                            int a = (lbp >> k) & 1;
                            int b = (lbp >> ((k + 1) % 8)) & 1;
                            if (a != b) transitions++;
                        }

                        int bin;
                        if (transitions <= 2) {
                            bin = Integer.bitCount(lbp);
                        } else {
                            bin = 9;
                        }
                        lbpHist[bin]++;
                        count++;
                    }
                }

                int base = (gy * GRID + gx) * LBP_BINS;
                double cellNorm = 0;
                for (int i = 0; i < LBP_BINS; i++) {
                    float val = count > 0 ? (float) lbpHist[i] / count : 0;
                    hist[base + i] = val;
                    cellNorm += val * val;
                }
                if (cellNorm > 1e-10) {
                    cellNorm = Math.sqrt(cellNorm);
                    for (int i = 0; i < LBP_BINS; i++) {
                        hist[base + i] /= cellNorm;
                    }
                }
            }
        }
        return hist;
    }

    private static float[] extractEdgeMagnitude(int[] pixels, int w, int h) {
        float[] mags = new float[GRID * GRID * MAG_BINS];
        for (int gy = 0; gy < GRID; gy++) {
            for (int gx = 0; gx < GRID; gx++) {
                double totalMag = 0;
                int count = 0;
                for (int y = 1; y < CELL - 1; y++) {
                    for (int x = 1; x < CELL - 1; x++) {
                        int px = gx * CELL + x;
                        int py = gy * CELL + y;
                        int tl = gray(pixels[(py - 1) * w + (px - 1)]);
                        int t  = gray(pixels[(py - 1) * w + px]);
                        int tr = gray(pixels[(py - 1) * w + (px + 1)]);
                        int l  = gray(pixels[py * w + (px - 1)]);
                        int r  = gray(pixels[py * w + (px + 1)]);
                        int bl = gray(pixels[(py + 1) * w + (px - 1)]);
                        int b  = gray(pixels[(py + 1) * w + px]);
                        int br = gray(pixels[(py + 1) * w + (px + 1)]);
                        int gxVal = (tr + 2 * r + br) - (tl + 2 * l + bl);
                        int gyVal = (tl + 2 * t + tr) - (bl + 2 * b + br);
                        totalMag += Math.sqrt(gxVal * gxVal + gyVal * gyVal);
                        count++;
                    }
                }
                mags[(gy * GRID + gx) * MAG_BINS] = count > 0 ? (float) (totalMag / count) / 255f : 0;
            }
        }
        return mags;
    }

    private static float[] rgbToLab(int rgb) {
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8) & 0xFF) / 255f;
        float b = (rgb & 0xFF) / 255f;

        float rLin = (r > 0.04045f) ? (float) Math.pow((r + 0.055) / 1.055, 2.4) : r / 12.92f;
        float gLin = (g > 0.04045f) ? (float) Math.pow((g + 0.055) / 1.055, 2.4) : g / 12.92f;
        float bLin = (b > 0.04045f) ? (float) Math.pow((b + 0.055) / 1.055, 2.4) : b / 12.92f;

        float x = 0.4124564f * rLin + 0.3575761f * gLin + 0.1804375f * bLin;
        float y = 0.2126729f * rLin + 0.7151522f * gLin + 0.0721750f * bLin;
        float z = 0.0193339f * rLin + 0.1191920f * gLin + 0.9503041f * bLin;

        float xn = 0.95047f, yn = 1.0f, zn = 1.08883f;
        float fx = labf(x / xn);
        float fy = labf(y / yn);
        float fz = labf(z / zn);

        float L = 116f * fy - 16f;
        float A = 500f * (fx - fy);
        float B = 200f * (fy - fz);

        return new float[]{L, A, B};
    }

    private static float labf(float t) {
        if (t > 0.008856f) return (float) Math.pow(t, 1.0 / 3.0);
        return (903.3f * t + 16f) / 116f;
    }

    private static float[] poolFeatures(float[] vec, int binCount, int poolFactor) {
        int oldGrid = (int) Math.sqrt(vec.length / binCount);
        int newGrid = oldGrid / poolFactor;
        float[] result = new float[newGrid * newGrid * binCount];

        for (int gy = 0; gy < newGrid; gy++) {
            for (int gx = 0; gx < newGrid; gx++) {
                int outBase = (gy * newGrid + gx) * binCount;
                for (int py = 0; py < poolFactor; py++) {
                    for (int px = 0; px < poolFactor; px++) {
                        int inBase = ((gy * poolFactor + py) * oldGrid + (gx * poolFactor + px)) * binCount;
                        for (int b = 0; b < binCount; b++) {
                            result[outBase + b] += vec[inBase + b];
                        }
                    }
                }
                float scale = 1.0f / (poolFactor * poolFactor);
                for (int b = 0; b < binCount; b++) {
                    result[outBase + b] *= scale;
                }
            }
        }
        return result;
    }

    private static float[] extractGlobalMoments(int[] pixels, int w, int h) {
        int n = w * h;
        double sumL = 0, sumA = 0, sumB = 0;
        double sumL2 = 0, sumA2 = 0, sumB2 = 0;

        int step = Math.max(1, n / 4096);
        int count = 0;
        for (int i = 0; i < n; i += step) {
            float[] lab = rgbToLab(pixels[i]);
            sumL += lab[0]; sumL2 += lab[0] * lab[0];
            sumA += lab[1]; sumA2 += lab[1] * lab[1];
            sumB += lab[2]; sumB2 += lab[2] * lab[2];
            count++;
        }

        double meanL = sumL / count, meanA = sumA / count, meanB = sumB / count;
        double stdL = Math.sqrt(Math.max(0, sumL2 / count - meanL * meanL));
        double stdA = Math.sqrt(Math.max(0, sumA2 / count - meanA * meanA));
        double stdB = Math.sqrt(Math.max(0, sumB2 / count - meanB * meanB));

        float[] moments = new float[]{
                (float) (meanL / 100f), (float) (meanA / 128f), (float) (meanB / 128f),
                (float) (stdL / 100f), (float) (stdA / 128f), (float) (stdB / 128f)
        };
        return moments;
    }

    private static float[] concat(float[][] arrays) {
        int total = 0;
        for (float[] a : arrays) total += a.length;
        float[] result = new float[total];
        int off = 0;
        for (float[] a : arrays) {
            System.arraycopy(a, 0, result, off, a.length);
            off += a.length;
        }
        return result;
    }

    private static void l2Normalize(float[] vec) {
        double norm = 0;
        for (float v : vec) norm += v * v;
        if (norm > 1e-10) {
            norm = Math.sqrt(norm);
            for (int i = 0; i < vec.length; i++) vec[i] /= norm;
        }
    }

    private static float[] scaleVec(float[] vec, float scale) {
        float[] result = new float[vec.length];
        for (int i = 0; i < vec.length; i++) result[i] = vec[i] * scale;
        return result;
    }

    private static BufferedImage toSrgb(BufferedImage src) {
        if (src.getColorModel().getColorSpace().isCS_sRGB() && src.getType() == BufferedImage.TYPE_INT_RGB) {
            return src;
        }
        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        SRGB_CONVERTER.filter(src, dest);
        return dest;
    }

    private static BufferedImage resize(BufferedImage source, int w, int h) {
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(source, 0, 0, w, h, null);
        g.dispose();
        return result;
    }

    private static int gray(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (r * 30 + g * 59 + b * 11) / 100;
    }
}
