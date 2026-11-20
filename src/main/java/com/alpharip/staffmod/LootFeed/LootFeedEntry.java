package com.alpharip.staffmod.LootFeed;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Aggregated kills and actual item drops for one entity type.
 */
public final class LootFeedEntry {
    private final String entityName;
    private final Map<String, Integer> drops = new LinkedHashMap<String, Integer>();
    private int killCount;
    private long lastUpdated;

    LootFeedEntry(String entityName) {
        this.entityName = entityName;
    }

    void addKill(long timestamp) {
        killCount++;
        lastUpdated = timestamp;
    }

    void addDrop(String itemName, int amount, long timestamp) {
        if (amount <= 0) {
            return;
        }

        Integer current = drops.get(itemName);
        drops.put(itemName, (current == null ? 0 : current) + amount);
        lastUpdated = timestamp;
    }

    public String getEntityName() {
        return entityName;
    }

    public int getKillCount() {
        return killCount;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public Map<String, Integer> getDrops() {
        return Collections.unmodifiableMap(drops);
    }
}
