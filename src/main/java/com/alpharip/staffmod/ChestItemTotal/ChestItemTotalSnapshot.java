package com.alpharip.staffmod.ChestItemTotal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable totals captured from one chest inventory snapshot. */
public final class ChestItemTotalSnapshot {
    private final List<ChestItemTotalEntry> entries;
    private final int totalItems;

    public ChestItemTotalSnapshot(List<ChestItemTotalEntry> entries, int totalItems) {
        this.entries = Collections.unmodifiableList(
                new ArrayList<ChestItemTotalEntry>(entries));
        this.totalItems = Math.max(0, totalItems);
    }

    public List<ChestItemTotalEntry> getEntries() { return entries; }
    public int getTotalItems() { return totalItems; }
    public int getUniqueItemTypes() { return entries.size(); }

    public static ChestItemTotalSnapshot empty() {
        return new ChestItemTotalSnapshot(Collections.<ChestItemTotalEntry>emptyList(), 0);
    }
}
