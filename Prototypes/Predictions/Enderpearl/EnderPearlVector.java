package com.alpharip.staffmod.predictions.enderpearl;

/** Immutable three-dimensional vector used by the Ender Pearl trajectory prototype. */
public final class EnderPearlVector {
    public final double x;
    public final double y;
    public final double z;

    public EnderPearlVector(double x, double y, double z) {
        if (!isFinite(x) || !isFinite(y) || !isFinite(z)) {
            throw new IllegalArgumentException("Vector components must be finite.");
        }
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public EnderPearlVector add(EnderPearlVector other) {
        if (other == null) throw new IllegalArgumentException("other");
        return new EnderPearlVector(x + other.x, y + other.y, z + other.z);
    }

    public EnderPearlVector scale(double factor) {
        if (!isFinite(factor)) throw new IllegalArgumentException("factor");
        return new EnderPearlVector(x * factor, y * factor, z * factor);
    }

    public double distanceTo(EnderPearlVector other) {
        if (other == null) throw new IllegalArgumentException("other");
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }
}
