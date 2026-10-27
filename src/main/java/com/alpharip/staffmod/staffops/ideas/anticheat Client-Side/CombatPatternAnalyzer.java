package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores manually supplied combat event labels and timestamps for later review. */
public final class CombatPatternAnalyzer {
    public static final class Event {
        private final long timestampMillis;
        private final String type;
        private final String detail;

        public Event(long timestampMillis, String type, String detail) {
            this.timestampMillis = timestampMillis;
            this.type = type == null ? "" : type.trim();
            this.detail = detail == null ? "" : detail.trim();
        }
        public long getTimestampMillis() { return timestampMillis; }
        public String getType() { return type; }
        public String getDetail() { return detail; }
    }

    private final List<Event> events = new ArrayList<Event>();
    public synchronized void add(Event event) { if (event != null) events.add(event); }
    public synchronized List<Event> getEvents() {
        return Collections.unmodifiableList(new ArrayList<Event>(events));
    }
    public synchronized void clear() { events.clear(); }
}