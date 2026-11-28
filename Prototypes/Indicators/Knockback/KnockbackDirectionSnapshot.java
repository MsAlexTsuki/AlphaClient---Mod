package com.alpharip.staffmod.indicators.knockback;

/** Immutable snapshot of one received horizontal knockback vector. */
public final class KnockbackDirectionSnapshot {
    private final double directionX;
    private final double directionZ;
    private final double normalizedX;
    private final double normalizedZ;
    private final double magnitude;
    private final long timestampMillis;

    KnockbackDirectionSnapshot(double directionX, double directionZ,
            double normalizedX, double normalizedZ, double magnitude,
            long timestampMillis) {
        this.directionX = directionX;
        this.directionZ = directionZ;
        this.normalizedX = normalizedX;
        this.normalizedZ = normalizedZ;
        this.magnitude = magnitude;
        this.timestampMillis = timestampMillis;
    }

    public double getDirectionX() {
        return directionX;
    }

    public double getDirectionZ() {
        return directionZ;
    }

    public double getNormalizedX() {
        return normalizedX;
    }

    public double getNormalizedZ() {
        return normalizedZ;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }
}
