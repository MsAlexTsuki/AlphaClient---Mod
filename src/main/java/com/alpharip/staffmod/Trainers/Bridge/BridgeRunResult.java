package com.alpharip.staffmod.trainers.bridge;

/** Immutable result snapshot for a completed bridge run. */
public final class BridgeRunResult {
    private final long startedAtMillis;
    private final long completedAtMillis;
    private final long elapsedMillis;
    private final int blocksPlaced;
    private final int misplacements;
    private final double blocksPerSecond;
    private final double accuracy;

    BridgeRunResult(long startedAtMillis, long completedAtMillis,
                    long elapsedMillis, int blocksPlaced, int misplacements,
                    double blocksPerSecond, double accuracy) {
        this.startedAtMillis = startedAtMillis;
        this.completedAtMillis = completedAtMillis;
        this.elapsedMillis = elapsedMillis;
        this.blocksPlaced = blocksPlaced;
        this.misplacements = misplacements;
        this.blocksPerSecond = blocksPerSecond;
        this.accuracy = accuracy;
    }

    public long getStartedAtMillis() {
        return startedAtMillis;
    }

    public long getCompletedAtMillis() {
        return completedAtMillis;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }

    public int getBlocksPlaced() {
        return blocksPlaced;
    }

    public int getMisplacements() {
        return misplacements;
    }

    public int getCorrectPlacements() {
        return blocksPlaced - misplacements;
    }

    public double getBlocksPerSecond() {
        return blocksPerSecond;
    }

    public double getAccuracy() {
        return accuracy;
    }
}
