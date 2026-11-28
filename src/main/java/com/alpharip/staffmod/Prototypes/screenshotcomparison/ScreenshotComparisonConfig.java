package com.alpharip.staffmod.screenshotcomparison;

/** In-memory prototype settings. Persistent config integration is not included. */
public final class ScreenshotComparisonConfig {
    private ScreenshotComparisonMode mode = ScreenshotComparisonMode.SIDE_BY_SIDE;
    private int differenceThreshold = 24;
    private float overlayAlpha = 0.5F;

    public ScreenshotComparisonMode getMode() {
        return mode;
    }

    public void setMode(ScreenshotComparisonMode mode) {
        if (mode != null) this.mode = mode;
    }

    public int getDifferenceThreshold() {
        return differenceThreshold;
    }

    public void setDifferenceThreshold(int threshold) {
        differenceThreshold = Math.max(0, Math.min(255, threshold));
    }

    public float getOverlayAlpha() {
        return overlayAlpha;
    }

    public void setOverlayAlpha(float alpha) {
        if (Float.isNaN(alpha) || Float.isInfinite(alpha)) return;
        overlayAlpha = Math.max(0.0F, Math.min(1.0F, alpha));
    }
}
