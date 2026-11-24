package com.alpharip.staffmod.Bestiary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** In-memory Bestiary containing only mob species the player has killed. */
public final class BestiaryBook {
    private final Map<String, BestiaryEntry> entries = new LinkedHashMap<String, BestiaryEntry>();

    public synchronized BestiaryEntry recordKill(String entityId, String displayName, String world, long timestamp) {
        if (entityId == null || entityId.trim().isEmpty()) {
            throw new IllegalArgumentException("entityId is required");
        }
        BestiaryEntry entry = entries.get(entityId);
        if (entry == null) {
            entry = new BestiaryEntry(entityId, displayName, world, timestamp);
            entries.put(entityId, entry);
        } else {
            entry.recordAnotherKill(timestamp);
        }
        return entry;
    }

    public synchronized List<BestiaryEntry> getEntries() {
        return Collections.unmodifiableList(new ArrayList<BestiaryEntry>(entries.values()));
    }

    public synchronized BestiaryEntry getEntry(String entityId) { return entries.get(entityId); }
    public synchronized int getDiscoveredSpeciesCount() { return entries.size(); }

    public synchronized int getTotalKills() {
        int total = 0;
        for (BestiaryEntry entry : entries.values()) {
            if (Integer.MAX_VALUE - total < entry.getKillCount()) return Integer.MAX_VALUE;
            total += entry.getKillCount();
        }
        return total;
    }

    public synchronized void clear() { entries.clear(); }
}
