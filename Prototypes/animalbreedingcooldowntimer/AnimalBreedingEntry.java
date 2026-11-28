package com.alpharip.staffmod.animalbreedingcooldowntimer;

import java.util.UUID;

/** Immutable cooldown snapshot for one animal. */
public final class AnimalBreedingEntry {
    private final UUID animalId;
    private final String displayName;
    private final long lastBredAtMillis;
    private final int cooldownSeconds;

    public AnimalBreedingEntry(UUID animalId, String displayName,
                               long lastBredAtMillis, int cooldownSeconds) {
        if (animalId == null) throw new IllegalArgumentException("animalId");
        this.animalId = animalId;
        this.displayName = displayName == null || displayName.trim().isEmpty()
                ? "Animal" : displayName;
        this.lastBredAtMillis = Math.max(0L, lastBredAtMillis);
        this.cooldownSeconds = Math.max(1, cooldownSeconds);
    }

    public UUID getAnimalId() { return animalId; }
    public String getDisplayName() { return displayName; }
    public long getLastBredAtMillis() { return lastBredAtMillis; }
    public int getCooldownSeconds() { return cooldownSeconds; }

    public int getRemainingSeconds(long nowMillis) {
        long elapsed = Math.max(0L, nowMillis - lastBredAtMillis) / 1000L;
        return (int) Math.max(0L, cooldownSeconds - elapsed);
    }

    public boolean isReady(long nowMillis) {
        return getRemainingSeconds(nowMillis) == 0;
    }
}
