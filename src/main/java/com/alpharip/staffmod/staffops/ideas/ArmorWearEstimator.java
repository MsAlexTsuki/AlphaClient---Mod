package com.alpharip.staffmod.staffops.ideas;
/** Foundation for estimating remaining opponent armor durability; runtime damage-event integration is pending. */
public final class ArmorWearEstimator {
    public int estimateRemaining(int maximumDamage, int accumulatedDamage) {
        if (maximumDamage <= 0) return 0;
        return Math.max(0, maximumDamage - Math.max(0, accumulatedDamage));
    }
}
