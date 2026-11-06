package com.alpharip.staffmod.analystics.target;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Immutable summary calculated from recorded target-switch events. */
public final class TargetSwitchStats {
    private final int switchCount;
    private final int uniqueTargetCount;
    private final double averageIntervalMillis;
    private final long shortestIntervalMillis;
    private final long longestIntervalMillis;

    private TargetSwitchStats(int switchCount, int uniqueTargetCount, double averageIntervalMillis,
                              long shortestIntervalMillis, long longestIntervalMillis) {
        this.switchCount = switchCount;
        this.uniqueTargetCount = uniqueTargetCount;
        this.averageIntervalMillis = averageIntervalMillis;
        this.shortestIntervalMillis = shortestIntervalMillis;
        this.longestIntervalMillis = longestIntervalMillis;
    }

    public static TargetSwitchStats from(List<TargetSwitchAnalyzer.SwitchEvent> events) {
        if (events == null || events.isEmpty()) return new TargetSwitchStats(0, 0, 0.0D, 0L, 0L);
        Set<String> targets = new HashSet<String>();
        long intervalTotal = 0L;
        long shortest = Long.MAX_VALUE;
        long longest = 0L;
        int intervals = 0;

        for (TargetSwitchAnalyzer.SwitchEvent event : events) {
            if (event.getPreviousTarget().length() > 0) targets.add(event.getPreviousTarget());
            if (event.getNextTarget().length() > 0) targets.add(event.getNextTarget());
        }
        for (int i = 1; i < events.size(); i++) {
            long interval = events.get(i).getTimestampMillis() - events.get(i - 1).getTimestampMillis();
            intervalTotal += interval;
            shortest = Math.min(shortest, interval);
            longest = Math.max(longest, interval);
            intervals++;
        }
        return new TargetSwitchStats(events.size(), targets.size(),
                intervals == 0 ? 0.0D : (double) intervalTotal / intervals,
                intervals == 0 ? 0L : shortest, longest);
    }

    public int getSwitchCount() { return switchCount; }
    public int getUniqueTargetCount() { return uniqueTargetCount; }
    public double getAverageIntervalMillis() { return averageIntervalMillis; }
    public long getShortestIntervalMillis() { return shortestIntervalMillis; }
    public long getLongestIntervalMillis() { return longestIntervalMillis; }
}
