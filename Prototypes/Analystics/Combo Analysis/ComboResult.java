package com.alpharip.staffmod.analystics.comboanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable analysis result for a completed fight. */
public final class ComboResult {
    private final long durationMillis;
    private final long maximumHitGapMillis;
    private final List<Long> hitTimes;
    private final ComboStats stats;

    ComboResult(long durationMillis, long maximumHitGapMillis, List<Long> hitTimes) {
        this.durationMillis = durationMillis;
        this.maximumHitGapMillis = maximumHitGapMillis;
        this.hitTimes = Collections.unmodifiableList(new ArrayList<Long>(hitTimes));
        this.stats = new ComboStats(this.hitTimes, maximumHitGapMillis);
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public long getMaximumHitGapMillis() {
        return maximumHitGapMillis;
    }

    public List<Long> getHitTimes() {
        return hitTimes;
    }

    public ComboStats getStats() {
        return stats;
    }
}
