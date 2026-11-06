package com.alpharip.staffmod.predictions.armor;

/** Immutable estimate for one opponent armor slot. */
public final class ArmorBreakResult {
    private final String opponentName;
    private final ArmorSlot slot;
    private final int maximumDurability;
    private final int accumulatedDamage;
    private final int remainingDurability;
    private final double remainingPercent;
    private final double averageDamagePerHit;
    private final int estimatedHitsToBreak;
    private final boolean predictionAvailable;
    private final boolean broken;

    public ArmorBreakResult(String opponentName, ArmorSlot slot, int maximumDurability,
                            int accumulatedDamage, int remainingDurability,
                            double remainingPercent, double averageDamagePerHit,
                            int estimatedHitsToBreak, boolean predictionAvailable, boolean broken) {
        this.opponentName = opponentName;
        this.slot = slot;
        this.maximumDurability = maximumDurability;
        this.accumulatedDamage = accumulatedDamage;
        this.remainingDurability = remainingDurability;
        this.remainingPercent = remainingPercent;
        this.averageDamagePerHit = averageDamagePerHit;
        this.estimatedHitsToBreak = estimatedHitsToBreak;
        this.predictionAvailable = predictionAvailable;
        this.broken = broken;
    }

    public String getOpponentName() { return opponentName; }
    public ArmorSlot getSlot() { return slot; }
    public int getMaximumDurability() { return maximumDurability; }
    public int getAccumulatedDamage() { return accumulatedDamage; }
    public int getRemainingDurability() { return remainingDurability; }
    public double getRemainingPercent() { return remainingPercent; }
    public double getAverageDamagePerHit() { return averageDamagePerHit; }
    public int getEstimatedHitsToBreak() { return estimatedHitsToBreak; }
    public boolean isPredictionAvailable() { return predictionAvailable; }
    public boolean isBroken() { return broken; }
}
