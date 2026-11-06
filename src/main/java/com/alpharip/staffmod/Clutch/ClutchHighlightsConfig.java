package com.alpharip.staffmod.clutch;

/** Immutable local configuration for clutch highlight collection. */
public final class ClutchHighlightsConfig {
    private final int maximumHighlights;

    public ClutchHighlightsConfig() {
        this(100);
    }

    public ClutchHighlightsConfig(int maximumHighlights) {
        if (maximumHighlights < 1) throw new IllegalArgumentException("maximumHighlights must be at least 1");
        this.maximumHighlights = maximumHighlights;
    }

    public int getMaximumHighlights() { return maximumHighlights; }
}
