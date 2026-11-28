package com.alpharip.staffmod.punishmentconflictdetector;

/** Punishment categories understood by this local prototype. */
public enum PunishmentType {
    BAN("Ban"),
    MUTE("Mute"),
    WARN("Warning"),
    KICK("Kick"),
    OTHER("Other");

    private final String displayName;

    PunishmentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
