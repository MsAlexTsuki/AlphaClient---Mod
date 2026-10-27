package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores supplied combat-related packet event summaries. */
public class CombatPacketAnalyzer {
    public static final class CombatPacketEvent {
        public final long timestampMillis;
        public final String eventType;
        public final String targetId;

        public CombatPacketEvent(long timestampMillis, String eventType, String targetId) {
            this.timestampMillis = timestampMillis;
            this.eventType = eventType == null ? "" : eventType;
            this.targetId = targetId == null ? "" : targetId;
        }
    }

    private final List<CombatPacketEvent> events = new ArrayList<CombatPacketEvent>();

    public void recordEvent(long timestampMillis, String eventType, String targetId) {
        events.add(new CombatPacketEvent(timestampMillis, eventType, targetId));
    }

    public List<CombatPacketEvent> getEvents() {
        return Collections.unmodifiableList(new ArrayList<CombatPacketEvent>(events));
    }

    public void clear() { events.clear(); }
}
