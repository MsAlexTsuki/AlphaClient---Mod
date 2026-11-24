package com.alpharip.staffmod;

/** Immutable memorial information for a deceased companion. */
public final class PetMemorialRecord {
    private final String petName;
    private final String species;
    private final String ownerName;
    private final String worldName;
    private final String cause;
    private final long deathTime;

    public PetMemorialRecord(String petName, String species, String ownerName,
                             String worldName, String cause, long deathTime) {
        this.petName = safe(petName, "Unnamed companion");
        this.species = safe(species, "Unknown species");
        this.ownerName = safe(ownerName, "Unknown owner");
        this.worldName = safe(worldName, "Unknown world");
        this.cause = safe(cause, "Cause unknown");
        this.deathTime = deathTime;
    }

    private static String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    public String getPetName() { return petName; }
    public String getSpecies() { return species; }
    public String getOwnerName() { return ownerName; }
    public String getWorldName() { return worldName; }
    public String getCause() { return cause; }
    public long getDeathTime() { return deathTime; }
}
