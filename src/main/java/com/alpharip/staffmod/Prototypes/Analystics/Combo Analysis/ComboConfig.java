package com.alpharip.staffmod.analystics.comboanalysis;

/** Configuration for grouping hits into combos. */
public final class ComboConfig {
    private final long maximumHitGapMillis;

    public ComboConfig() {
        this(500L);
    }

    public ComboConfig(long maximumHitGapMillis) {
        if (maximumHitGapMillis < 0L) {
            throw new IllegalArgumentException("maximumHitGapMillis must not be negative.");
        }
        this.maximumHitGapMillis = maximumHitGapMillis;
    }

    public long getMaximumHitGapMillis() {
        return maximumHitGapMillis;
    }
}
