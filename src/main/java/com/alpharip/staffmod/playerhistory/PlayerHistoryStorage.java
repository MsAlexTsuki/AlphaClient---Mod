package com.alpharip.staffmod.playerhistory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/** Local-only Properties storage. No account, network, or Supabase access. */
public final class PlayerHistoryStorage {
    public void load(File file, PlayerHistoryStore store) {
        if (file == null || store == null || !file.isFile()) return;
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(file)) {
            properties.load(input);
            List<PlayerHistoryEntry> entries = new ArrayList<PlayerHistoryEntry>();
            int entryCount = parseCount(properties.getProperty("entry.count"));
            for (int i = 0; i < entryCount; i++) {
                String prefix = "entry." + i + ".";
                try {
                    UUID id = UUID.fromString(properties.getProperty(prefix + "uuid"));
                    String name = properties.getProperty(prefix + "name", "Unknown");
                    long first = parseLong(properties.getProperty(prefix + "first"), 0L);
                    long last = parseLong(properties.getProperty(prefix + "last"), first);
                    long total = parseLong(properties.getProperty(prefix + "total"), 0L);
                    int sessions = (int) parseLong(properties.getProperty(prefix + "sessions"), 0L);
                    // A process restart cannot prove that a previously active session continued.
                    entries.add(new PlayerHistoryEntry(id, name, first, last, total,
                            sessions, false, 0L));
                } catch (RuntimeException ignored) {
                    // Skip malformed individual records without discarding valid history.
                }
            }

            List<PlayerHistoryEvent> events = new ArrayList<PlayerHistoryEvent>();
            int eventCount = Math.min(PlayerHistoryStore.MAX_EVENTS,
                    parseCount(properties.getProperty("event.count")));
            for (int i = 0; i < eventCount; i++) {
                String prefix = "event." + i + ".";
                try {
                    UUID id = UUID.fromString(properties.getProperty(prefix + "uuid"));
                    String name = properties.getProperty(prefix + "name", "Unknown");
                    PlayerHistoryEvent.Type type = PlayerHistoryEvent.Type.valueOf(
                            properties.getProperty(prefix + "type"));
                    long timestamp = parseLong(properties.getProperty(prefix + "time"), 0L);
                    String context = properties.getProperty(prefix + "context", "Unknown");
                    events.add(new PlayerHistoryEvent(id, name, type, timestamp, context));
                } catch (RuntimeException ignored) {
                    // Skip malformed individual events.
                }
            }
            store.restore(entries, events);
        } catch (IOException ignored) {
            // Keep the in-memory store available if local storage cannot be read.
        }
    }

    public void save(File file, PlayerHistoryStore store) {
        if (file == null || store == null) return;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.isDirectory()) return;

        Properties properties = new Properties();
        List<PlayerHistoryEntry> entries = store.getEntriesSnapshot();
        List<PlayerHistoryEvent> events = store.getEventsSnapshot();
        long now = System.currentTimeMillis();
        properties.setProperty("entry.count", String.valueOf(entries.size()));
        for (int i = 0; i < entries.size(); i++) {
            PlayerHistoryEntry entry = entries.get(i);
            String prefix = "entry." + i + ".";
            properties.setProperty(prefix + "uuid", entry.getPlayerId().toString());
            properties.setProperty(prefix + "name", entry.getPlayerName());
            properties.setProperty(prefix + "first", String.valueOf(entry.getFirstSeenAt()));
            properties.setProperty(prefix + "last", String.valueOf(entry.getLastSeenAt()));
            properties.setProperty(prefix + "total", String.valueOf(entry.getTotalOnlineMillisAt(now)));
            properties.setProperty(prefix + "sessions", String.valueOf(entry.getSessionCount()));
        }

        properties.setProperty("event.count", String.valueOf(events.size()));
        for (int i = 0; i < events.size(); i++) {
            PlayerHistoryEvent event = events.get(i);
            String prefix = "event." + i + ".";
            properties.setProperty(prefix + "uuid", event.getPlayerId().toString());
            properties.setProperty(prefix + "name", event.getPlayerName());
            properties.setProperty(prefix + "type", event.getType().name());
            properties.setProperty(prefix + "time", String.valueOf(event.getTimestamp()));
            properties.setProperty(prefix + "context", event.getConnectionContext());
        }

        File temporary = new File(file.getParentFile(), file.getName() + ".tmp");
        try {
            try (FileOutputStream output = new FileOutputStream(temporary)) {
                properties.store(output, "Alpha Client Player History - local data");
            }
            try {
                Files.move(temporary.toPath(), file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicMoveUnavailable) {
                Files.move(temporary.toPath(), file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ignored) {
            // A failed save must not crash the client; the current session remains in memory.
        } finally {
            if (temporary.exists()) temporary.delete();
        }
    }

    private static int parseCount(String value) {
        return (int) Math.max(0L, Math.min(100000L, parseLong(value, 0L)));
    }

    private static long parseLong(String value, long fallback) {
        if (value == null) return fallback;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
