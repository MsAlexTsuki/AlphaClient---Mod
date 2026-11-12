package com.alpharip.staffmod.trainers.potionthrowcoach;

/** Settings for the local Potion Throw Coach prototype. */
public final class PotionThrowCoachConfig {
    private int maximumHistoryEntries = 100;
    private double accurateLandingRadius = 1.5D;

    public synchronized int getMaximumHistoryEntries() {
        return maximumHistoryEntries;
    }

    public synchronized void setMaximumHistoryEntries(int maximum) {
        if (maximum < 1) {
            throw new IllegalArgumentException("maximumHistoryEntries must be >= 1.");
        }
        maximumHistoryEntries = maximum;
    }

    public synchronized double getAccurateLandingRadius() {
        return accurateLandingRadius;
    }

    public synchronized void setAccurateLandingRadius(double radius) {
        if (!isFinite(radius) || radius <= 0.0D) {
            throw new IllegalArgumentException("accurateLandingRadius must be finite and > 0.");
        }
        accurateLandingRadius = radius;
    }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }
}
