package com.alpharip.staffmod.staffops.ideas;

/**
 * Calculates average clicks per second from clicks recorded during combat.
 * Combat-state detection and click event hooks must be connected separately.
 */
public final class AverageCombatCps {
    private long combatClickCount;
    private long combatDurationMillis;

    public void recordCombatClick() {
        combatClickCount++;
    }

    public void addCombatDuration(long durationMillis) {
        if (durationMillis > 0L) {
            combatDurationMillis += durationMillis;
        }
    }

    public double getAverageCps() {
        if (combatDurationMillis <= 0L) {
            return 0.0D;
        }
        return combatClickCount * 1000.0D / combatDurationMillis;
    }

    public long getCombatClickCount() {
        return combatClickCount;
    }

    public long getCombatDurationMillis() {
        return combatDurationMillis;
    }

    public void reset() {
        combatClickCount = 0L;
        combatDurationMillis = 0L;
    }
}
