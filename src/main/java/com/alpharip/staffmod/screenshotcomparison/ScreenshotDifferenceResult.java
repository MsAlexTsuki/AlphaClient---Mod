package com.alpharip.staffmod.screenshotcomparison;

import java.awt.image.BufferedImage;

/** Pixel-difference image and summary metrics for a screenshot pair. */
public final class ScreenshotDifferenceResult {
    private final BufferedImage image;
    private final long comparedPixels;
    private final long changedPixels;
    private final double meanChannelDifference;

    ScreenshotDifferenceResult(BufferedImage image, long comparedPixels,
                                long changedPixels, double meanChannelDifference) {
        this.image = image;
        this.comparedPixels = comparedPixels;
        this.changedPixels = changedPixels;
        this.meanChannelDifference = meanChannelDifference;
    }

    public BufferedImage getImage() {
        return image;
    }

    public long getComparedPixels() {
        return comparedPixels;
    }

    public long getChangedPixels() {
        return changedPixels;
    }

    public double getChangedPercentage() {
        return comparedPixels == 0L ? 0.0D : changedPixels * 100.0D / comparedPixels;
    }

    public double getMeanChannelDifference() {
        return meanChannelDifference;
    }
}
