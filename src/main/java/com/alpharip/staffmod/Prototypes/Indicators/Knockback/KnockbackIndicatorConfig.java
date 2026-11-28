package com.alpharip.staffmod.indicators.knockback;

/** Immutable settings for the Knockback Direction Indicator prototype. */
public final class KnockbackIndicatorConfig {
    private final long visibleDurationMillis;
    private final double minimumMagnitude;

    public KnockbackIndicatorConfig() {
        this(1200L, 0.01D);
    }

    public KnockbackIndicatorConfig(long visibleDurationMillis, double minimumMagnitude) {
        if (visibleDurationMillis < 0L) {
            throw new IllegalArgumentException("visibleDurationMillis must not be negative.");
        }
        if (Double.isNaN(minimumMagnitude) || Double.isInfinite(minimumMagnitude)
                || minimumMagnitude < 0.0D) {
            throw new IllegalArgumentException("minimumMagnitude must be finite and non-negative.");
        }
        this.visibleDurationMillis = visibleDurationMillis;
        this.minimumMagnitude = minimumMagnitude;
    }

    public long getVisibleDurationMillis() {
        return visibleDurationMillis;
    }

    public double getMinimumMagnitude() {
        return minimumMagnitude;
    }
}
