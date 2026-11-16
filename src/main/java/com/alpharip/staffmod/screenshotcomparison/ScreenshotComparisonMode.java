package com.alpharip.staffmod.screenshotcomparison;

public enum ScreenshotComparisonMode {
    SIDE_BY_SIDE("Side by side"),
    OVERLAY("Overlay"),
    DIFFERENCE("Difference");

    private final String displayName;

    ScreenshotComparisonMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ScreenshotComparisonMode next() {
        ScreenshotComparisonMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
