package com.alpharip.staffmod.trainers.bridge;

/**
 * Represents one manually started and finished bridge run.
 * Placement events must be supplied by a future Minecraft/Forge integration.
 */
public final class BridgeRun {
    private final long startedAtMillis;
    private final long startedAtNanos;
    private final BridgeRunStats stats = new BridgeRunStats();

    private boolean finished;
    private BridgeRunResult result;

    BridgeRun() {
        this.startedAtMillis = System.currentTimeMillis();
        this.startedAtNanos = System.nanoTime();
    }

    public synchronized void recordBlockPlacement(boolean misplaced) {
        ensureActive();
        stats.recordPlacement(misplaced);
    }

    public synchronized BridgeRunResult finish() {
        if (result != null) {
            return result;
        }

        long completedAtMillis = System.currentTimeMillis();
        long elapsedNanos = Math.max(0L, System.nanoTime() - startedAtNanos);
        long elapsedMillis = elapsedNanos / 1000000L;
        double elapsedSeconds = elapsedNanos / 1000000000.0D;
        int blocksPlaced = stats.getBlocksPlaced();

        double blocksPerSecond = elapsedSeconds <= 0.0D
                ? 0.0D
                : blocksPlaced / elapsedSeconds;

        result = new BridgeRunResult(
                startedAtMillis,
                completedAtMillis,
                elapsedMillis,
                blocksPlaced,
                stats.getMisplacements(),
                blocksPerSecond,
                stats.getAccuracy()
        );
        finished = true;
        return result;
    }

    public synchronized boolean isFinished() {
        return finished;
    }

    public synchronized int getBlocksPlaced() {
        return stats.getBlocksPlaced();
    }

    public synchronized int getMisplacements() {
        return stats.getMisplacements();
    }

    public synchronized int getCorrectPlacements() {
        return stats.getCorrectPlacements();
    }

    public synchronized double getAccuracy() {
        return stats.getAccuracy();
    }

    private void ensureActive() {
        if (finished) {
            throw new IllegalStateException("This bridge run has finished.");
        }
    }
}
