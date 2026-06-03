package com.app.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class ScreenshotPreprocessor {

    private static final ColorConvertOp SRGB_CONVERTER = new ColorConvertOp(
            ColorSpace.getInstance(ColorSpace.CS_sRGB), null);

    private static final Kernel GAUSSIAN_KERNEL;
    static {
        float[] data = {
            0.0113f, 0.0838f, 0.0113f,
            0.0838f, 0.6193f, 0.0838f,
            0.0113f, 0.0838f, 0.0113f
        };
        GAUSSIAN_KERNEL = new Kernel(3, 3, data);
    }

    private static final ConvolveOp GAUSSIAN_OP = new ConvolveOp(
            GAUSSIAN_KERNEL, ConvolveOp.EDGE_NO_OP, null);

    public static BufferedImage preprocess(BufferedImage source) {
        BufferedImage result = toSrgb(source);
        result = applyGaussianBlur(result);
        result = enhanceResolution(result);
        return result;
    }

    private static BufferedImage toSrgb(BufferedImage src) {
        if (src.getColorModel().getColorSpace().isCS_sRGB()
                && src.getType() == BufferedImage.TYPE_INT_RGB) {
            return src;
        }
        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        SRGB_CONVERTER.filter(src, dest);
        return dest;
    }

    private static BufferedImage applyGaussianBlur(BufferedImage src) {
        if (src.getType() != BufferedImage.TYPE_INT_RGB) {
            BufferedImage rgb = new BufferedImage(src.getWidth(), src.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgb.createGraphics();
            g.drawImage(src, 0, 0, null);
            g.dispose();
            return GAUSSIAN_OP.filter(rgb, null);
        }
        return GAUSSIAN_OP.filter(src, null);
    }

    private static BufferedImage enhanceResolution(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        if (w >= 224 && h >= 224) return src;

        double scale = Math.max(224.0 / w, 224.0 / h);
        int newW = (int) Math.ceil(w * scale);
        int newH = (int) Math.ceil(h * scale);

        BufferedImage result = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, newW, newH, null);
        g.dispose();
        return result;
    }
}
