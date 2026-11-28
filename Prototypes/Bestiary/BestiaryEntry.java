package com.alpharip.staffmod.Bestiary;

/** One mob species unlocked by the player's first credited kill. */
public final class BestiaryEntry {
    private final String entityId;
    private final String displayName;
    private final String firstWorld;
    private final long firstKillAt;
    private long lastKillAt;
    private int killCount;

    public BestiaryEntry(String entityId, String displayName, String world, long timestamp) {
        this.entityId = require(entityId, "entityId");
        this.displayName = require(displayName, "displayName");
        this.firstWorld = require(world, "world");
        this.firstKillAt = timestamp;
        this.lastKillAt = timestamp;
        this.killCount = 1;
    }

    void recordAnotherKill(long timestamp) {
        if (killCount < Integer.MAX_VALUE) killCount++;
        lastKillAt = timestamp;
    }

    public String getEntityId() { return entityId; }
    public String getDisplayName() { return displayName; }
    public String getFirstWorld() { return firstWorld; }
    public long getFirstKillAt() { return firstKillAt; }
    public long getLastKillAt() { return lastKillAt; }
    public int getKillCount() { return killCount; }

    private static String require(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }
}
