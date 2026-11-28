package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores timestamped activity notes supplied by a detection workflow. */
public class SuspiciousActivityTimeline {
    public static class Entry {
        private final long timestamp;
        private final String description;

        public Entry(long timestamp, String description) {
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalArgumentException("description cannot be blank");
            }
            this.timestamp = timestamp;
            this.description = description.trim();
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getDescription() {
            return description;
        }
    }

    private final List<Entry> entries = new ArrayList<Entry>();

    public void add(Entry entry) {
        if (entry != null) {
            entries.add(entry);
        }
    }

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(new ArrayList<Entry>(entries));
    }

    public void clear() {
        entries.clear();
    }
}
