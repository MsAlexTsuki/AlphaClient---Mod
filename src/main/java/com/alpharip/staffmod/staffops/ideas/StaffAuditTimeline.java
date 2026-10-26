package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * In-memory timeline model for locally supplied audit events.
 * TODO(Supabase): define trusted event timestamps and append-only audit storage
 * before showing shared or authoritative timeline data.
 */
public final class StaffAuditTimeline {
    public static final String FEATURE_NAME = "Staff Audit Timeline";
    private final List<Event> events = new ArrayList<Event>();

    public void addEvent(long timestampMillis, String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Event description cannot be blank");
        }
        events.add(new Event(timestampMillis, description.trim()));
    }

    public List<Event> getChronologicalEvents() {
        List<Event> copy = new ArrayList<Event>(events);
        Collections.sort(copy, new Comparator<Event>() {
            @Override
            public int compare(Event left, Event right) {
                return Long.compare(left.getTimestampMillis(), right.getTimestampMillis());
            }
        });
        return Collections.unmodifiableList(copy);
    }

    public static final class Event {
        private final long timestampMillis;
        private final String description;

        private Event(long timestampMillis, String description) {
            this.timestampMillis = timestampMillis;
            this.description = description;
        }

        public long getTimestampMillis() { return timestampMillis; }
        public Date getDate() { return new Date(timestampMillis); }
        public String getDescription() { return description; }
    }
}
