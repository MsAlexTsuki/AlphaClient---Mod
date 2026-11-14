package com.alpharip.staffmod.rememberchestinventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stored snapshot of a chest at a dimension and block position. */
public final class ChestMemoryEntry {
    private final int dimensionId;
    private final int x;
    private final int y;
    private final int z;
    private final String chestName;
    private final long observedAt;
    private final List<ChestItemSnapshot> items;

    public ChestMemoryEntry(int dimensionId, int x, int y, int z, String chestName,
                            long observedAt, List<ChestItemSnapshot> items) {
        this.dimensionId = dimensionId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.chestName = chestName == null ? "Chest" : chestName;
        this.observedAt = Math.max(0L, observedAt);
        this.items = Collections.unmodifiableList(new ArrayList<ChestItemSnapshot>(
                items == null ? Collections.<ChestItemSnapshot>emptyList() : items));
    }

    public String getKey() { return dimensionId + ":" + x + ":" + y + ":" + z; }
    public int getDimensionId() { return dimensionId; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public String getChestName() { return chestName; }
    public long getObservedAt() { return observedAt; }
    public List<ChestItemSnapshot> getItems() { return items; }
}