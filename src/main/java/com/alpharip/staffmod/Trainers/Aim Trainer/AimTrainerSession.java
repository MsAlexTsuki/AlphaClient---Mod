package com.alpharip.staffmod.trainers.aimtrainer;

/**
 * Runs one local, in-memory Aim Trainer session.
 *
 * Aim offsets are normalized to the range -1.0..1.0 and aggregated by
 * AimDriftHeatmap. Minecraft input sampling and screen rendering are not
 * connected in this prototype; callers must provide the samples.
 */
public final class AimTrainerSession {
    private final AimTrainerConfig config;
    private final AimTrainerStats stats = new AimTrainerStats();
    private final AimDriftHeatmap heatmap;
    private final long startedAtNanos;

    private long finishedAtNanos;
    private boolean finished;
    private AimTrainerResult result;

    public AimTrainerSession() {
        this(new AimTrainerConfig());
    }

    public AimTrainerSession(AimTrainerConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null.");
        }
        this.config = config;
        this.heatmap = new AimDriftHeatmap(config.getHeatmapGridSize());
        this.startedAtNanos = System.nanoTime();
    }

    public synchronized void recordAimOffset(double horizontal, double vertical) {
        ensureActive();
        heatmap.record(horizontal, vertical);
    }

    public synchronized void recordHit() {
        ensureActive();
        stats.recordHit();
    }

    public synchronized void recordMiss() {
        ensureActive();
        stats.recordMiss();
    }

    public synchronized AimTrainerResult finish() {
        if (result != null) {
            return result;
        }

        finishedAtNanos = System.nanoTime();
        finished = true;
        long elapsedMillis = Math.max(0L,
                (finishedAtNanos - startedAtNanos) / 1000000L);

        result = new AimTrainerResult(
                stats.getHits(),
                stats.getMisses(),
                stats.getAccuracy(),
                elapsedMillis,
                heatmap.getSamples(),
                heatmap.snapshot()
        );
        return result;
    }

    public synchronized boolean isFinished() {
        return finished;
    }

    public synchronized int getHits() {
        return stats.getHits();
    }

    public synchronized int getMisses() {
        return stats.getMisses();
    }

    public synchronized double getAccuracy() {
        return stats.getAccuracy();
    }

    public synchronized long getHeatmapSamples() {
        return heatmap.getSamples();
    }

    public AimTrainerConfig getConfig() {
        return config;
    }

    private void ensureActive() {
        if (finished) {
            throw new IllegalStateException("This Aim Trainer session has finished.");
        }
    }
}
