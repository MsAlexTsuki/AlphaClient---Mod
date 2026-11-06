package com.alpharip.staffmod.trainers.aimtrainer;

/**
 * Hit and miss counters for one Aim Trainer session.
 */
public final class AimTrainerStats {
    private int hits;
    private int misses;

    public synchronized void recordHit() {
        hits++;
    }

    public synchronized void recordMiss() {
        misses++;
    }

    public synchronized int getHits() {
        return hits;
    }

    public synchronized int getMisses() {
        return misses;
    }

    public synchronized int getAttempts() {
        return hits + misses;
    }

    public synchronized double getAccuracy() {
        int attempts = hits + misses;
        return attempts == 0 ? 0.0D : (hits * 100.0D) / attempts;
    }
}
