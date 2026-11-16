package com.alpharip.staffmod.punishmentconflictdetector;

/** Immutable pair of simultaneously active punishments for the same target and type. */
public final class PunishmentConflict {
    private final PunishmentRecord first;
    private final PunishmentRecord second;

    public PunishmentConflict(PunishmentRecord first, PunishmentRecord second) {
        if (first == null || second == null) {
            throw new IllegalArgumentException("Both punishment records are required.");
        }
        this.first = first;
        this.second = second;
    }

    public PunishmentRecord getFirst() { return first; }
    public PunishmentRecord getSecond() { return second; }

    public String getSummary() {
        return first.getType().getDisplayName() + " overlap for " + first.getTargetName();
    }
}
