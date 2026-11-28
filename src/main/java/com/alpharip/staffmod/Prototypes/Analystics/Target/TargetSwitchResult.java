package com.alpharip.staffmod.analystics.target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable snapshot of a completed target-switch analysis session. */
public final class TargetSwitchResult {
    private final long startedAtMillis;
    private final long finishedAtMillis;
    private final List<TargetSwitchAnalyzer.SwitchEvent> events;
    private final TargetSwitchStats stats;

    public TargetSwitchResult(long startedAtMillis, long finishedAtMillis,
                              List<TargetSwitchAnalyzer.SwitchEvent> events) {
        if (startedAtMillis < 0L || finishedAtMillis < startedAtMillis)
            throw new IllegalArgumentException("Invalid session time range");
        if (events == null) throw new IllegalArgumentException("events cannot be null");
        this.startedAtMillis = startedAtMillis;
        this.finishedAtMillis = finishedAtMillis;
        this.events = Collections.unmodifiableList(
                new ArrayList<TargetSwitchAnalyzer.SwitchEvent>(events));
        this.stats = TargetSwitchStats.from(this.events);
    }

    public long getStartedAtMillis() { return startedAtMillis; }
    public long getFinishedAtMillis() { return finishedAtMillis; }
    public long getDurationMillis() { return finishedAtMillis - startedAtMillis; }
    public List<TargetSwitchAnalyzer.SwitchEvent> getEvents() { return events; }
    public TargetSwitchStats getStats() { return stats; }
}
