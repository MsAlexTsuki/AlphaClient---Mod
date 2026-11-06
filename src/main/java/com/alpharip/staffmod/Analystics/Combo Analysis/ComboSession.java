package com.alpharip.staffmod.analystics.comboanalysis;

/**
 * In-memory lifecycle for one fight analysis session.
 *
 * A future Forge event adapter can submit hit timestamps in milliseconds
 * relative to the start of the fight. This class does not read game events.
 */
public final class ComboSession {
    private final ComboConfig config;
    private final ComboAnalysisAfterFight analysis = new ComboAnalysisAfterFight();
    private final long startedAtNanos;
    private boolean finished;
    private boolean cancelled;
    private ComboResult result;

    public ComboSession(ComboConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null.");
        }
        this.config = config;
        this.startedAtNanos = System.nanoTime();
    }

    /** Records a hit using elapsed milliseconds since this session started. */
    public synchronized void recordHitAt(long elapsedMillis) {
        ensureActive();
        analysis.recordHit(elapsedMillis);
    }

    /** Records a hit at the current elapsed session time. */
    public synchronized void recordHitNow() {
        ensureActive();
        long elapsedMillis = Math.max(0L, (System.nanoTime() - startedAtNanos) / 1000000L);
        analysis.recordHit(elapsedMillis);
    }

    /** Finishes the session and returns an immutable result. */
    public synchronized ComboResult finish() {
        if (cancelled) {
            throw new IllegalStateException("A cancelled combo analysis session cannot be finished.");
        }
        if (result != null) {
            return result;
        }

        ensureActive();
        long durationMillis = Math.max(0L, (System.nanoTime() - startedAtNanos) / 1000000L);
        result = new ComboResult(
                durationMillis,
                config.getMaximumHitGapMillis(),
                analysis.getHitTimes()
        );
        finished = true;
        return result;
    }

    /** Discards collected hit data and closes this session. */
    public synchronized void cancel() {
        ensureActive();
        analysis.clear();
        cancelled = true;
        finished = true;
    }

    public synchronized boolean isFinished() {
        return finished;
    }

    public synchronized int getHitCount() {
        return analysis.getHitCount();
    }

    public ComboConfig getConfig() {
        return config;
    }

    private void ensureActive() {
        if (finished) {
            throw new IllegalStateException("This combo analysis session has finished.");
        }
    }
}
