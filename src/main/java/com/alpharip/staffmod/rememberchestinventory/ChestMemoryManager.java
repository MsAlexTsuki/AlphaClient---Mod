package com.alpharip.staffmod.rememberchestinventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Owns the bounded in-memory chest history. */
public final class ChestMemoryManager {
    private final Map<String, ChestMemoryEntry> entries =
            new LinkedHashMap<String, ChestMemoryEntry>();
    private final ChestMemorySearch search = new ChestMemorySearch();
    private int maximumRememberedChests = 100;
    private boolean enabled = true;

    public synchronized void remember(ChestMemoryEntry entry) {
        if (!enabled || entry == null) return;
        entries.remove(entry.getKey());
        entries.put(entry.getKey(), entry);
        trim();
    }

    public synchronized List<ChestMemoryEntry> getEntries() {
        return Collections.unmodifiableList(new ArrayList<ChestMemoryEntry>(entries.values()));
    }

    public synchronized List<ChestMemoryEntry> search(String query) {
        return search.filter(getEntries(), query);
    }

    public synchronized void clear() { entries.clear(); }

    public synchronized void setMaximumRememberedChests(int maximum) {
        maximumRememberedChests = Math.max(1, Math.min(10000, maximum));
        trim();
    }

    public synchronized int getMaximumRememberedChests() { return maximumRememberedChests; }
    public synchronized boolean isEnabled() { return enabled; }
    public synchronized void setEnabled(boolean enabled) { this.enabled = enabled; }
    public synchronized int size() { return entries.size(); }

    public synchronized void replaceAll(List<ChestMemoryEntry> loaded) {
        entries.clear();
        if (loaded != null) {
            for (ChestMemoryEntry entry : loaded) {
                if (entry != null) {
                    entries.remove(entry.getKey());
                    entries.put(entry.getKey(), entry);
                }
            }
        }
        trim();
    }

    private void trim() {
        while (entries.size() > maximumRememberedChests) {
            String oldest = entries.keySet().iterator().next();
            entries.remove(oldest);
        }
    }
}