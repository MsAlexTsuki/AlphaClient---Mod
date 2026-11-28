package com.alpharip.staffmod.trainers.bridge;

/** Mutable counters for one bridge run. */
public final class BridgeRunStats {
    private int blocksPlaced;
    private int misplacements;

    synchronized void recordPlacement(boolean misplaced) {
        blocksPlaced++;
        if (misplaced) {
            misplacements++;
        }
    }

    public synchronized int getBlocksPlaced() {
        return blocksPlaced;
    }

    public synchronized int getMisplacements() {
        return misplacements;
    }

    public synchronized int getCorrectPlacements() {
        return blocksPlaced - misplacements;
    }

    public synchronized double getAccuracy() {
        return blocksPlaced == 0
                ? 0.0D
                : (getCorrectPlacements() * 100.0D) / blocksPlaced;
    }
}
