package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Local tracker for join events observed by the client, such as visible chat/system messages.
 * It cannot guarantee a complete server-wide join log.
 */
public final class PlayerJoinTracker {
    private final List<Event> events = new ArrayList<Event>();

    public void record(String playerName, long timestampMillis) {
        events.add(new Event(playerName, timestampMillis));
    }

    public List<Event> getEvents() {
        return Collections.unmodifiableList(new ArrayList<Event>(events));
    }

    public void clear() { events.clear(); }

    public static final class Event {
        private final String playerName;
        private final long timestampMillis;

        public Event(String playerName, long timestampMillis) {
            this.playerName = playerName == null ? "" : playerName;
            this.timestampMillis = timestampMillis;
        }

        public String getPlayerName() { return playerName; }
        public long getTimestampMillis() { return timestampMillis; }
    }
}