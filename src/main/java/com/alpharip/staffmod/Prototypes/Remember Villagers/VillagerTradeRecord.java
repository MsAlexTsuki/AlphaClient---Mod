package com.alpharip.staffmod.remembervillagers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable local snapshot of the trades offered by one villager. */
public final class VillagerTradeRecord {
    private final String villagerId;
    private final String villagerName;
    private final int dimensionId;
    private final int x;
    private final int y;
    private final int z;
    private final long capturedAt;
    private final List<VillagerTradeEntry> trades;

    public VillagerTradeRecord(String villagerId, String villagerName, int dimensionId,
                               int x, int y, int z, long capturedAt,
                               List<VillagerTradeEntry> trades) {
        if (villagerId == null || trades == null) {
            throw new IllegalArgumentException("Villager ID and trades are required");
        }
        this.villagerId = villagerId;
        this.villagerName = villagerName == null || villagerName.trim().isEmpty()
                ? "Villager" : villagerName;
        this.dimensionId = dimensionId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.capturedAt = capturedAt;
        this.trades = Collections.unmodifiableList(new ArrayList<VillagerTradeEntry>(trades));
    }

    public String getVillagerId() { return villagerId; }
    public String getVillagerName() { return villagerName; }
    public int getDimensionId() { return dimensionId; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public long getCapturedAt() { return capturedAt; }
    public List<VillagerTradeEntry> getTrades() { return trades; }

    public boolean hasSameTrades(List<VillagerTradeEntry> other) {
        if (other == null || trades.size() != other.size()) return false;
        for (int i = 0; i < trades.size(); i++) {
            if (!trades.get(i).sameAs(other.get(i))) return false;
        }
        return true;
    }
}
