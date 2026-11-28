package com.alpharip.staffmod.screenshotcomparison;

import java.awt.Color;
import java.awt.image.BufferedImage;

/** Compares normalized pixels; differing image dimensions are scaled to a shared canvas. */
public final class ScreenshotDifferenceAnalyzer {
    private static final long MAX_COMPARISON_PIXELS = 40000000L;

    public ScreenshotDifferenceResult analyze(ScreenshotComparisonPair pair, int threshold) {
        if (pair == null) throw new IllegalArgumentException("A screenshot pair is required.");
        int safeThreshold = Math.max(0, Math.min(255, threshold));
        BufferedImage before = pair.getBefore().getImage();
        BufferedImage after = pair.getAfter().getImage();
        int width = Math.max(before.getWidth(), after.getWidth());
        int height = Math.max(before.getHeight(), after.getHeight());
        long total = (long) width * (long) height;
        if (total <= 0L || total > MAX_COMPARISON_PIXELS) {
            throw new IllegalArgumentException("The normalized comparison canvas exceeds 40 megapixels.");
        }
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        long changed = 0L;
        long differenceSum = 0L;
        for (int y = 0; y < height; y++) {
            int by = Math.min(before.getHeight() - 1, (int) ((long) y * before.getHeight() / height));
            int ay = Math.min(after.getHeight() - 1, (int) ((long) y * after.getHeight() / height));
            for (int x = 0; x < width; x++) {
                int bx = Math.min(before.getWidth() - 1, (int) ((long) x * before.getWidth() / width));
                int ax = Math.min(after.getWidth() - 1, (int) ((long) x * after.getWidth() / width));
                Color a = new Color(before.getRGB(bx, by), true);
                Color b = new Color(after.getRGB(ax, ay), true);
                int delta = (Math.abs(a.getRed() - b.getRed())
                        + Math.abs(a.getGreen() - b.getGreen())
                        + Math.abs(a.getBlue() - b.getBlue())) / 3;
                differenceSum += delta;
                if (delta > safeThreshold) changed++;
                int red = Math.min(255, delta * 3);
                int green = Math.max(0, 80 - delta);
                int blue = Math.max(0, 40 - delta / 2);
                output.setRGB(x, y, 0xFF000000 | (red << 16) | (green << 8) | blue);
            }
        }
        return new ScreenshotDifferenceResult(output, total, changed,
                total == 0L ? 0.0D : (double) differenceSum / (double) total);
    }
}
