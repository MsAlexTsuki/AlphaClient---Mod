package com.alpharip.staffmod.rememberchestinventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Search helpers for chest names, coordinates, and remembered item names/IDs. */
public final class ChestMemorySearch {
    public List<ChestMemoryEntry> filter(List<ChestMemoryEntry> entries, String query) {
        if (entries == null || entries.isEmpty()) return Collections.emptyList();
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (needle.isEmpty()) return new ArrayList<ChestMemoryEntry>(entries);

        List<ChestMemoryEntry> matches = new ArrayList<ChestMemoryEntry>();
        for (ChestMemoryEntry entry : entries) {
            if (entry == null) continue;
            if (contains(entry.getChestName(), needle)
                    || contains(entry.getKey(), needle)) {
                matches.add(entry);
                continue;
            }
            for (ChestItemSnapshot item : entry.getItems()) {
                if (contains(item.getDisplayName(), needle) || contains(item.getItemId(), needle)) {
                    matches.add(entry);
                    break;
                }
            }
        }
        return matches;
    }

    private boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }
}