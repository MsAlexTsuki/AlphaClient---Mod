package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Records caller-supplied target changes without tracking players in live gameplay. */
public final class TargetSwitchAnalyzer {
    public static final class SwitchEvent {
        private final long timestampMillis;
        private final String previousTarget;
        private final String nextTarget;

        public SwitchEvent(long timestampMillis, String previousTarget, String nextTarget) {
            this.timestampMillis = timestampMillis;
            this.previousTarget = previousTarget == null ? "" : previousTarget.trim();
            this.nextTarget = nextTarget == null ? "" : nextTarget.trim();
        }
        public long getTimestampMillis() { return timestampMillis; }
        public String getPreviousTarget() { return previousTarget; }
        public String getNextTarget() { return nextTarget; }
    }

    private final List<SwitchEvent> events = new ArrayList<SwitchEvent>();
    public synchronized void add(SwitchEvent event) { if (event != null) events.add(event); }
    public synchronized List<SwitchEvent> getEvents() {
        return Collections.unmodifiableList(new ArrayList<SwitchEvent>(events));
    }
    public synchronized void clear() { events.clear(); }
}