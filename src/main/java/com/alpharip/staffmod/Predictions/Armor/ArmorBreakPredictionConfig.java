package com.alpharip.staffmod.predictions.armor;

/** Immutable settings for local armor durability sampling and prediction. */
public final class ArmorBreakPredictionConfig {
    private final int minimumSamples;
    private final int maxSamplesPerSlot;

    public ArmorBreakPredictionConfig(int minimumSamples, int maxSamplesPerSlot) {
        if (minimumSamples < 1) {
            throw new IllegalArgumentException("minimumSamples must be at least 1.");
        }
        if (maxSamplesPerSlot < minimumSamples) {
            throw new IllegalArgumentException("maxSamplesPerSlot must be at least minimumSamples.");
        }
        this.minimumSamples = minimumSamples;
        this.maxSamplesPerSlot = maxSamplesPerSlot;
    }

    public static ArmorBreakPredictionConfig defaults() {
        return new ArmorBreakPredictionConfig(2, 20);
    }

    public int getMinimumSamples() { return minimumSamples; }
    public int getMaxSamplesPerSlot() { return maxSamplesPerSlot; }
}
