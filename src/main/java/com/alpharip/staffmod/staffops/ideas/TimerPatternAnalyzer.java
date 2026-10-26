package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Summarizes caller-supplied intervals without making cheat determinations. */
public class TimerPatternAnalyzer {
    private final List<Long> intervalsMillis = new ArrayList<Long>();

    public void recordInterval(long intervalMillis) {
        if (intervalMillis >= 0L) intervalsMillis.add(intervalMillis);
    }

    public List<Long> getIntervalsMillis() {
        return Collections.unmodifiableList(new ArrayList<Long>(intervalsMillis));
    }

    public double getAverageIntervalMillis() {
        if (intervalsMillis.isEmpty()) return 0.0D;
        double total = 0.0D;
        for (Long interval : intervalsMillis) total += interval.longValue();
        return total / intervalsMillis.size();
    }

    public void clear() { intervalsMillis.clear(); }
}
