package com.alpharip.staffmod.ChestItemTotal;

/** Aggregated amount for one item type in the currently viewed chest. */
public final class ChestItemTotalEntry {
    private final String itemId;
    private final String displayName;
    private final int count;

    public ChestItemTotalEntry(String itemId, String displayName, int count) {
        this.itemId = itemId == null ? "" : itemId;
        this.displayName = displayName == null ? this.itemId : displayName;
        this.count = Math.max(0, count);
    }

    public String getItemId() { return itemId; }
    public String getDisplayName() { return displayName; }
    public int getCount() { return count; }
}
