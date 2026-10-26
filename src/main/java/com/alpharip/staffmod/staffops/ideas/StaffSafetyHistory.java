package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Client-side model for reviewing locally recorded staff-safety events.
 * This class does not enforce safety controls or persist data remotely.
 */
public final class StaffSafetyHistory {
    private final List<Entry> entries = new ArrayList<Entry>();

    public void record(String eventType, String detail, long timestampMillis) {
        entries.add(new Entry(eventType, detail, timestampMillis));
    }

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(new ArrayList<Entry>(entries));
    }

    public void clearLocalHistory() {
        entries.clear();
    }

    public static final class Entry {
        private final String eventType;
        private final String detail;
        private final long timestampMillis;

        public Entry(String eventType, String detail, long timestampMillis) {
            this.eventType = eventType == null ? "" : eventType;
            this.detail = detail == null ? "" : detail;
            this.timestampMillis = timestampMillis;
        }

        public String getEventType() { return eventType; }
        public String getDetail() { return detail; }
        public long getTimestampMillis() { return timestampMillis; }
    }

    // TODO(Supabase): define approved storage and access rules before synchronizing history.
}