package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores timestamped debug entries. */
public class DetectionDebugTimeline {
    public static class Entry {
        private final long timestamp;
        private final String message;
        public Entry(long timestamp, String message) {
            this.timestamp = timestamp;
            this.message = message == null ? "" : message;
        }
        public long getTimestamp() { return timestamp; }
        public String getMessage() { return message; }
    }
    private final List<Entry> entries = new ArrayList<Entry>();
    public void add(long timestamp, String message) { entries.add(new Entry(timestamp, message)); }
    public List<Entry> snapshot() {
        return Collections.unmodifiableList(new ArrayList<Entry>(entries));
    }
    public void clear() { entries.clear(); }
}