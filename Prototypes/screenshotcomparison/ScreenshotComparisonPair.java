package com.alpharip.staffmod.screenshotcomparison;

/** Holds the before/after images for one comparison. */
public final class ScreenshotComparisonPair {
    private final ScreenshotComparisonImage before;
    private final ScreenshotComparisonImage after;

    public ScreenshotComparisonPair(ScreenshotComparisonImage before, ScreenshotComparisonImage after) {
        if (before == null || after == null) {
            throw new IllegalArgumentException("Both before and after screenshots are required.");
        }
        this.before = before;
        this.after = after;
    }

    public ScreenshotComparisonImage getBefore() {
        return before;
    }

    public ScreenshotComparisonImage getAfter() {
        return after;
    }
}
