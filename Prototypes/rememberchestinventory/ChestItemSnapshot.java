package com.alpharip.staffmod.rememberchestinventory;

/** Immutable summary of one item stack observed in a chest. */
public final class ChestItemSnapshot {
    private final String itemId;
    private final String displayName;
    private final int metadata;
    private final int count;

    public ChestItemSnapshot(String itemId, String displayName, int metadata, int count) {
        this.itemId = itemId == null ? "" : itemId;
        this.displayName = displayName == null ? "" : displayName;
        this.metadata = Math.max(0, metadata);
        this.count = Math.max(0, count);
    }

    public String getItemId() { return itemId; }
    public String getDisplayName() { return displayName; }
    public int getMetadata() { return metadata; }
    public int getCount() { return count; }
}