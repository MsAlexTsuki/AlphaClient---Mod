package com.alpharip.staffmod.timing.blockbreak;

/** Immutable settings for local block-break timing collection. */
public final class BlockBreakTimingConfig {
    private final long slowThresholdMillis;
    private final int maximumSamples;

    public BlockBreakTimingConfig() {
        this(250L, 200);
    }

    public BlockBreakTimingConfig(long slowThresholdMillis, int maximumSamples) {
        if (slowThresholdMillis < 0L) throw new IllegalArgumentException("slowThresholdMillis cannot be negative");
        if (maximumSamples < 1) throw new IllegalArgumentException("maximumSamples must be at least 1");
        this.slowThresholdMillis = slowThresholdMillis;
        this.maximumSamples = maximumSamples;
    }

    public long getSlowThresholdMillis() { return slowThresholdMillis; }
    public int getMaximumSamples() { return maximumSamples; }
}
