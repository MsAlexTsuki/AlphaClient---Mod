package com.alpharip.staffmod.timing.blockplacement;

/** Immutable settings for the Block Placement Timing prototype. */
public final class BlockPlacementTimingConfig {
    private final long slowPlacementThresholdMillis;
    private final int maxSamples;

    public BlockPlacementTimingConfig() {
        this(250L, 200);
    }

    public BlockPlacementTimingConfig(long slowPlacementThresholdMillis, int maxSamples) {
        if (slowPlacementThresholdMillis < 0L) {
            throw new IllegalArgumentException("slowPlacementThresholdMillis cannot be negative");
        }
        if (maxSamples < 1) {
            throw new IllegalArgumentException("maxSamples must be at least 1");
        }
        this.slowPlacementThresholdMillis = slowPlacementThresholdMillis;
        this.maxSamples = maxSamples;
    }

    public long getSlowPlacementThresholdMillis() {
        return slowPlacementThresholdMillis;
    }

    public int getMaxSamples() {
        return maxSamples;
    }
}
