package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Records item yields attributed to a mob grinder.
 * Mob-drop detection and storage/export integration must be added separately.
 */
public final class MobGrinderYieldLog {
    private final List<YieldEntry> entries = new ArrayList<YieldEntry>();

    public void recordYield(String itemId, int quantity, long timestampMillis) {
        if (itemId == null || itemId.trim().isEmpty() || quantity <= 0) {
            return;
        }
        entries.add(new YieldEntry(itemId, quantity, timestampMillis));
    }

    public List<YieldEntry> getEntries() {
        return Collections.unmodifiableList(new ArrayList<YieldEntry>(entries));
    }

    public long getTotalQuantity() {
        long total = 0L;
        for (YieldEntry entry : entries) {
            total += entry.getQuantity();
        }
        return total;
    }

    public void clear() {
        entries.clear();
    }

    public static final class YieldEntry {
        private final String itemId;
        private final int quantity;
        private final long timestampMillis;

        public YieldEntry(String itemId, int quantity, long timestampMillis) {
            this.itemId = itemId;
            this.quantity = quantity;
            this.timestampMillis = timestampMillis;
        }

        public String getItemId() { return itemId; }
        public int getQuantity() { return quantity; }
        public long getTimestampMillis() { return timestampMillis; }
    }
}
