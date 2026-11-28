package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores event records provided by another component. */
public class DetectionEventCapture {
    public static class EventRecord {
        private final String type;
        private final long timestamp;
        private final String details;

        public EventRecord(String type, long timestamp, String details) {
            if (type == null || type.trim().isEmpty()) {
                throw new IllegalArgumentException("type cannot be blank");
            }
            this.type = type.trim();
            this.timestamp = timestamp;
            this.details = details == null ? "" : details;
        }

        public String getType() { return type; }
        public long getTimestamp() { return timestamp; }
        public String getDetails() { return details; }
    }

    private final List<EventRecord> records = new ArrayList<EventRecord>();

    public void add(EventRecord record) {
        if (record != null) {
            records.add(record);
        }
    }

    public List<EventRecord> getRecords() {
        return Collections.unmodifiableList(new ArrayList<EventRecord>(records));
    }

    public void clear() {
        records.clear();
    }
}
