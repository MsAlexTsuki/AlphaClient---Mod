package com.alpharip.staffmod.staffops.ideas;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
/** Rolling evidence buffer; target capture hooks, hotkey and report export are pending. */
public final class EvidenceClipper {
    public static final long WINDOW_MILLIS = 30000L;
    public static final class Event {
        public final long timestamp;
        public final String type, details;
        public Event(long timestamp, String type, String details) {
            this.timestamp = timestamp; this.type = type == null ? "unknown" : type;
            this.details = details == null ? "" : details;
        }
    }
    private final Deque<Event> events = new ArrayDeque<Event>();
    public void record(Event event, long now) { if (event != null) { events.addLast(event); prune(now); } }
    private void prune(long now) { while (!events.isEmpty() && now - events.peekFirst().timestamp > WINDOW_MILLIS) events.removeFirst(); }
    public List<Event> snapshot(long now) { prune(now); return Collections.unmodifiableList(new ArrayList<Event>(events)); }
    public void clear() { events.clear(); }
}
