package com.alpharip.staffmod.predictions.armor;

/** Snapshot of one equipped armor piece that crossed the warning threshold. */
public final class ArmorDurabilityWarningPiece {
    private final ArmorSlot slot;
    private final String displayName;
    private final int remainingDurability;
    private final int maximumDurability;
    private final double remainingPercent;

    ArmorDurabilityWarningPiece(ArmorSlot slot, String displayName, int remainingDurability,
                                int maximumDurability, double remainingPercent) {
        this.slot = slot;
        this.displayName = displayName;
        this.remainingDurability = remainingDurability;
        this.maximumDurability = maximumDurability;
        this.remainingPercent = remainingPercent;
    }

    public ArmorSlot getSlot() { return slot; }
    public String getDisplayName() { return displayName; }
    public int getRemainingDurability() { return remainingDurability; }
    public int getMaximumDurability() { return maximumDurability; }
    public double getRemainingPercent() { return remainingPercent; }
}
