package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Local record of connection transitions observed by this client.
 * It does not detect reconnects that happen outside the client's observation window.
 */
public final class PlayerReconnectTracker {
    private final List<ConnectionEvent> events = new ArrayList<ConnectionEvent>();

    public void record(String state, long timestampMillis) {
        events.add(new ConnectionEvent(state, timestampMillis));
    }

    public List<ConnectionEvent> getEvents() {
        return Collections.unmodifiableList(new ArrayList<ConnectionEvent>(events));
    }

    public void clear() { events.clear(); }

    public static final class ConnectionEvent {
        private final String state;
        private final long timestampMillis;

        public ConnectionEvent(String state, long timestampMillis) {
            this.state = state == null ? "Unknown" : state;
            this.timestampMillis = timestampMillis;
        }

        public String getState() { return state; }
        public long getTimestampMillis() { return timestampMillis; }
    }
}