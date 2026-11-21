package com.alpharip.staffmod.basestorageledger;

/** Aggregated stock information derived from the latest remembered chest snapshots. */
public final class StorageLedgerItem {
    private final String itemId;
    private final int metadata;
    private final String displayName;
    private final int totalCount;
    private final int chestCount;
    private final long lastObservedAt;

    StorageLedgerItem(String itemId, int metadata, String displayName, int totalCount,
                      int chestCount, long lastObservedAt) {
        this.itemId = itemId;
        this.metadata = metadata;
        this.displayName = displayName;
        this.totalCount = totalCount;
        this.chestCount = chestCount;
        this.lastObservedAt = lastObservedAt;
    }

    public String getKey() { return itemId + "#" + metadata; }
    public String getItemId() { return itemId; }
    public int getMetadata() { return metadata; }
    public String getDisplayName() { return displayName; }
    public int getTotalCount() { return totalCount; }
    public int getChestCount() { return chestCount; }
    public long getLastObservedAt() { return lastObservedAt; }

    public boolean isStale(long now, long staleAfterMillis) {
        return lastObservedAt <= 0L || now - lastObservedAt > staleAfterMillis;
    }
}
