package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Summarizes intervals from supplied click timestamps; does not label a player as cheating. */
public final class AutoClickPatternDetector {
    public List<Long> intervalsMillis(List<Long> timestamps) {
        List<Long> intervals = new ArrayList<Long>();
        if (timestamps == null || timestamps.size() < 2) return intervals;
        for (int i = 1; i < timestamps.size(); i++) {
            Long previous = timestamps.get(i - 1);
            Long current = timestamps.get(i);
            if (previous != null && current != null) intervals.add(current - previous);
        }
        return Collections.unmodifiableList(intervals);
    }

    public double averageIntervalMillis(List<Long> timestamps) {
        List<Long> intervals = intervalsMillis(timestamps);
        if (intervals.isEmpty()) return 0.0D;
        double total = 0.0D;
        for (Long interval : intervals) total += interval;
        return total / intervals.size();
    }
}