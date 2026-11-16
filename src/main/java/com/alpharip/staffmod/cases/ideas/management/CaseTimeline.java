package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Local chronological event list for a case; it is not an authoritative audit log. */
public final class CaseTimeline {
    private final List<Event> events = new ArrayList<Event>();

    public void add(String type, String description, long timestampMillis) {
        events.add(new Event(type, description, timestampMillis));
    }

    public List<Event> getChronologicalEvents() {
        List<Event> result = new ArrayList<Event>(events);
        Collections.sort(result, new Comparator<Event>() {
            @Override
            public int compare(Event a, Event b) {
                return Long.compare(a.timestampMillis, b.timestampMillis);
            }
        });
        return Collections.unmodifiableList(result);
    }

    public static final class Event {
        private final String type;
        private final String description;
        private final long timestampMillis;

        public Event(String type, String description, long timestampMillis) {
            this.type = type == null ? "Note" : type;
            this.description = description == null ? "" : description;
            this.timestampMillis = timestampMillis;
        }

        public String getType() { return type; }
        public String getDescription() { return description; }
        public long getTimestampMillis() { return timestampMillis; }
    }

    // TODO(Supabase): use an authoritative append-only source if this becomes a shared audit timeline.
}