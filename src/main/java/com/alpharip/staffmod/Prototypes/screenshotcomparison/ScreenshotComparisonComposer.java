package com.alpharip.staffmod.screenshotcomparison;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** Builds the image displayed by the comparison screen. */
public final class ScreenshotComparisonComposer {
    private static final long MAX_OUTPUT_PIXELS = 40000000L;
    private final ScreenshotDifferenceAnalyzer analyzer = new ScreenshotDifferenceAnalyzer();

    public BufferedImage compose(ScreenshotComparisonPair pair, ScreenshotComparisonConfig config) {
        if (pair == null || config == null) return null;
        BufferedImage before = pair.getBefore().getImage();
        BufferedImage after = pair.getAfter().getImage();
        ScreenshotComparisonMode mode = config.getMode();

        if (mode == ScreenshotComparisonMode.DIFFERENCE) {
            return analyzer.analyze(pair, config.getDifferenceThreshold()).getImage();
        }

        if (mode == ScreenshotComparisonMode.OVERLAY) {
            int width = Math.max(before.getWidth(), after.getWidth());
            int height = Math.max(before.getHeight(), after.getHeight());
            ensureSafeOutput(width, height);
            BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = result.createGraphics();
            try {
                graphics.setColor(Color.DARK_GRAY);
                graphics.fillRect(0, 0, width, height);
                graphics.drawImage(before, 0, 0, null);
                graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                        config.getOverlayAlpha()));
                graphics.drawImage(after, 0, 0, width, height, null);
            } finally {
                graphics.dispose();
            }
            return result;
        }

        int width = before.getWidth() + after.getWidth() + 12;
        int height = Math.max(before.getHeight(), after.getHeight());
        ensureSafeOutput(width, height);
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = result.createGraphics();
        try {
            graphics.setColor(new Color(18, 20, 26));
            graphics.fillRect(0, 0, width, height);
            graphics.drawImage(before, 0, 0, null);
            graphics.setColor(new Color(255, 200, 61));
            graphics.fillRect(before.getWidth() + 5, 0, 2, height);
            graphics.drawImage(after, before.getWidth() + 12, 0, null);
        } finally {
            graphics.dispose();
        }
        return result;
    }

    public ScreenshotDifferenceResult analyze(ScreenshotComparisonPair pair,
                                               ScreenshotComparisonConfig config) {
        return analyzer.analyze(pair, config.getDifferenceThreshold());
    }

    private static void ensureSafeOutput(int width, int height) {
        long pixels = (long) width * (long) height;
        if (pixels <= 0L || pixels > MAX_OUTPUT_PIXELS) {
            throw new IllegalArgumentException("The composed preview exceeds 40 megapixels.");
        }
    }
}
