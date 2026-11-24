package com.alpharip.staffmod;

import java.util.Date;

/** One mob type recorded by the Bestiary prototype. */
public final class BestiaryEntry {
    private final String entityId;
    private final String displayName;
    private final String firstWorld;
    private final long firstEncounter;
    private long lastEncounter;
    private int encounterCount;

    public BestiaryEntry(String entityId, String displayName, String world, long timestamp) {
        this.entityId = require(entityId, "entityId");
        this.displayName = require(displayName, "displayName");
        this.firstWorld = require(world, "world");
        this.firstEncounter = timestamp;
        this.lastEncounter = timestamp;
        this.encounterCount = 1;
    }

    void encounteredAgain(long timestamp) {
        lastEncounter = timestamp;
        encounterCount++;
    }

    public String getEntityId() { return entityId; }
    public String getDisplayName() { return displayName; }
    public String getFirstWorld() { return firstWorld; }
    public long getFirstEncounter() { return firstEncounter; }
    public long getLastEncounter() { return lastEncounter; }
    public int getEncounterCount() { return encounterCount; }

    private static String require(String value, String field) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(field + " is required");
        return value;
    }
}
