package com.alpharip.staffmod.staffops.ideas;
/** Eating/drinking progress calculation; player tracking and HUD rendering are pending. */
public final class EatingRing {
    public double getProgress(long startedAt, long now, long durationMillis) {
        if (durationMillis <= 0L) return 1D;
        return Math.max(0D, Math.min(1D, (double)(now - startedAt) / durationMillis));
    }
}
