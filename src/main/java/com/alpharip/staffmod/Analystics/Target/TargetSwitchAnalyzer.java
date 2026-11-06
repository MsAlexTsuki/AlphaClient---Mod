package com.alpharip.staffmod.analystics.target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Records target changes supplied by a caller; live Minecraft target detection is not included. */
public final class TargetSwitchAnalyzer {
    public static final class SwitchEvent {
        private final long timestampMillis;
        private final String previousTarget;
        private final String nextTarget;

        public SwitchEvent(long timestampMillis, String previousTarget, String nextTarget) {
            if (timestampMillis < 0L) throw new IllegalArgumentException("timestampMillis must be non-negative");
            this.timestampMillis = timestampMillis;
            this.previousTarget = normalize(previousTarget);
            this.nextTarget = normalize(nextTarget);
        }

        public long getTimestampMillis() { return timestampMillis; }
        public String getPreviousTarget() { return previousTarget; }
        public String getNextTarget() { return nextTarget; }

        private static String normalize(String value) {
            return value == null ? "" : value.trim();
        }
    }

    private final List<SwitchEvent> events = new ArrayList<SwitchEvent>();
    private final TargetSwitchConfig config;

    public TargetSwitchAnalyzer() { this(new TargetSwitchConfig()); }

    public TargetSwitchAnalyzer(TargetSwitchConfig config) {
        if (config == null) throw new IllegalArgumentException("config cannot be null");
        this.config = config;
    }

    public synchronized void add(SwitchEvent event) {
        if (event == null) return;
        if (!events.isEmpty()
                && event.getTimestampMillis() < events.get(events.size() - 1).getTimestampMillis()) {
            throw new IllegalArgumentException("Events must be added in timestamp order");
        }
        events.add(event);
        while (events.size() > config.getMaxEvents()) events.remove(0);
    }

    public synchronized List<SwitchEvent> getEvents() {
        return Collections.unmodifiableList(new ArrayList<SwitchEvent>(events));
    }

    public synchronized TargetSwitchStats getStats() {
        return TargetSwitchStats.from(getEvents());
    }

    public TargetSwitchConfig getConfig() { return config; }

    public synchronized void clear() { events.clear(); }
}
