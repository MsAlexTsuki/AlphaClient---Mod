package com.alpharip.staffmod.analystics.comboanalysis;

import java.util.List;

/** Immutable statistics calculated from one completed fight. */
public final class ComboStats {
    private final int hitCount;
    private final int comboCount;
    private final int longestCombo;
    private final long averageHitIntervalMillis;
    private final long largestHitGapMillis;

    ComboStats(List<Long> hitTimes, long maximumHitGapMillis) {
        int hits = hitTimes.size();
        int combos = 0;
        int longest = 0;
        int current = 0;
        long intervalTotal = 0L;
        long largestGap = 0L;

        for (int i = 0; i < hits; i++) {
            if (i == 0) {
                combos = 1;
                current = 1;
                longest = 1;
                continue;
            }

            long gap = hitTimes.get(i) - hitTimes.get(i - 1);
            intervalTotal += gap;
            largestGap = Math.max(largestGap, gap);

            if (gap <= maximumHitGapMillis) {
                current++;
            } else {
                combos++;
                current = 1;
            }
            longest = Math.max(longest, current);
        }

        this.hitCount = hits;
        this.comboCount = combos;
        this.longestCombo = longest;
        this.averageHitIntervalMillis = hits < 2 ? 0L : intervalTotal / (hits - 1);
        this.largestHitGapMillis = largestGap;
    }

    public int getHitCount() {
        return hitCount;
    }

    public int getComboCount() {
        return comboCount;
    }

    public int getLongestCombo() {
        return longestCombo;
    }

    public long getAverageHitIntervalMillis() {
        return averageHitIntervalMillis;
    }

    public long getLargestHitGapMillis() {
        return largestHitGapMillis;
    }
}
