package com.alpharip.staffmod.analystics.target;

/** In-memory session that accepts manually supplied target-switch events. */
public final class TargetSwitchSession {
    private final long startedAtMillis;
    private final TargetSwitchAnalyzer analyzer;
    private boolean finished;

    public TargetSwitchSession(long startedAtMillis, TargetSwitchConfig config) {
        if (startedAtMillis < 0L) throw new IllegalArgumentException("startedAtMillis must be non-negative");
        this.startedAtMillis = startedAtMillis;
        this.analyzer = new TargetSwitchAnalyzer(config == null ? new TargetSwitchConfig() : config);
    }

    public synchronized void recordSwitch(long timestampMillis, String previousTarget, String nextTarget) {
        if (finished) throw new IllegalStateException("Session is already finished");
        if (timestampMillis < startedAtMillis)
            throw new IllegalArgumentException("Switch cannot occur before the session starts");
        analyzer.add(new TargetSwitchAnalyzer.SwitchEvent(timestampMillis, previousTarget, nextTarget));
    }

    public synchronized TargetSwitchResult finish(long finishedAtMillis) {
        if (finished) throw new IllegalStateException("Session is already finished");
        if (finishedAtMillis < startedAtMillis)
            throw new IllegalArgumentException("Finish time cannot be before session start");
        finished = true;
        return new TargetSwitchResult(startedAtMillis, finishedAtMillis, analyzer.getEvents());
    }

    public synchronized boolean isFinished() { return finished; }
    public synchronized TargetSwitchStats getStats() { return analyzer.getStats(); }
    public synchronized void cancel() {
        if (finished) throw new IllegalStateException("Session is already finished");
        finished = true;
        analyzer.clear();
    }
}
