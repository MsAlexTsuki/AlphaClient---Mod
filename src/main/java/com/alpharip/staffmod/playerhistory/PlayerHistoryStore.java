package com.alpharip.staffmod.playerhistory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Owns player summaries and a bounded local event timeline. */
public final class PlayerHistoryStore {
    public static final int MAX_EVENTS = 500;

    private final Map<UUID, PlayerHistoryEntry> entries =
            new LinkedHashMap<UUID, PlayerHistoryEntry>();
    private final List<PlayerHistoryEvent> events = new ArrayList<PlayerHistoryEvent>();

    public synchronized void startSession(UUID id, String name, String context,
                                           long now, PlayerHistoryEvent.Type type) {
        PlayerHistoryEntry old = entries.get(id);
        if (old == null) {
            old = new PlayerHistoryEntry(id, name, now, now, 0L, 0, false, 0L);
        }
        if (old.isOnline()) {
            entries.put(id, new PlayerHistoryEntry(id, name, old.getFirstSeenAt(),
                    now, old.getTotalOnlineMillis(), old.getSessionCount(), true,
                    old.getCurrentSessionStartedAt()));
            return;
        }
        entries.put(id, new PlayerHistoryEntry(id, name, old.getFirstSeenAt(), now,
                old.getTotalOnlineMillis(), old.getSessionCount() + 1, true, now));
        addEvent(new PlayerHistoryEvent(id, name, type, now, context));
    }

    public synchronized void endSession(UUID id, String context, long now) {
        PlayerHistoryEntry old = entries.get(id);
        if (old == null || !old.isOnline()) return;
        long duration = Math.max(0L, now - old.getCurrentSessionStartedAt());
        entries.put(id, new PlayerHistoryEntry(id, old.getPlayerName(),
                old.getFirstSeenAt(), now, old.getTotalOnlineMillis() + duration,
                old.getSessionCount(), false, 0L));
        addEvent(new PlayerHistoryEvent(id, old.getPlayerName(),
                PlayerHistoryEvent.Type.LEFT, now, context));
    }

    public synchronized void updateName(UUID id, String name, long now) {
        PlayerHistoryEntry old = entries.get(id);
        if (old == null || name == null || name.trim().isEmpty()
                || name.equals(old.getPlayerName())) return;
        entries.put(id, new PlayerHistoryEntry(id, name, old.getFirstSeenAt(),
                now, old.getTotalOnlineMillis(), old.getSessionCount(),
                old.isOnline(), old.getCurrentSessionStartedAt()));
    }

    public synchronized void closeAll(long now, String context) {
        List<UUID> active = new ArrayList<UUID>();
        for (PlayerHistoryEntry entry : entries.values()) {
            if (entry.isOnline()) active.add(entry.getPlayerId());
        }
        for (UUID id : active) endSession(id, context, now);
    }

    private void addEvent(PlayerHistoryEvent event) {
        events.add(event);
        while (events.size() > MAX_EVENTS) events.remove(0);
    }

    public synchronized List<PlayerHistoryEntry> getEntriesSnapshot() {
        List<PlayerHistoryEntry> result =
                new ArrayList<PlayerHistoryEntry>(entries.values());
        Collections.sort(result, new Comparator<PlayerHistoryEntry>() {
            @Override
            public int compare(PlayerHistoryEntry a, PlayerHistoryEntry b) {
                return Long.compare(b.getLastSeenAt(), a.getLastSeenAt());
            }
        });
        return Collections.unmodifiableList(result);
    }

    public synchronized List<PlayerHistoryEvent> getEventsSnapshot() {
        return Collections.unmodifiableList(new ArrayList<PlayerHistoryEvent>(events));
    }

    public synchronized void restore(Collection<PlayerHistoryEntry> loadedEntries,
                                     Collection<PlayerHistoryEvent> loadedEvents) {
        entries.clear();
        events.clear();
        if (loadedEntries != null) {
            for (PlayerHistoryEntry entry : loadedEntries) {
                if (entry != null) entries.put(entry.getPlayerId(), entry);
            }
        }
        if (loadedEvents != null) {
            for (PlayerHistoryEvent event : loadedEvents) {
                if (event != null) addEvent(event);
            }
        }
    }

    public synchronized void clear() {
        entries.clear();
        events.clear();
    }
}
