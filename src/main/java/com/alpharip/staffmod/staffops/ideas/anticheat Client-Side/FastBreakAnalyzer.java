package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores supplied block-break durations for later review. */
public class FastBreakAnalyzer {
    private final List<Long> breakDurationsMillis = new ArrayList<Long>();

    public void recordBreakDuration(long durationMillis) {
        if (durationMillis >= 0L) breakDurationsMillis.add(durationMillis);
    }

    public List<Long> getBreakDurationsMillis() {
        return Collections.unmodifiableList(new ArrayList<Long>(breakDurationsMillis));
    }

    public long getShortestBreakDurationMillis() {
        if (breakDurationsMillis.isEmpty()) return -1L;
        long shortest = Long.MAX_VALUE;
        for (Long duration : breakDurationsMillis) shortest = Math.min(shortest, duration.longValue());
        return shortest;
    }

    public void clear() { breakDurationsMillis.clear(); }
}
