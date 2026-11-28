package com.alpharip.staffmod.timing.blockplacement;

import java.util.List;

/** Immutable summary of collected block-placement intervals. */
public final class BlockPlacementTimingStats {
    private final int sampleCount;
    private final int slowPlacementCount;
    private final double averageIntervalMillis;
    private final long fastestIntervalMillis;
    private final long slowestIntervalMillis;
    private final double standardDeviationMillis;

    BlockPlacementTimingStats(List<BlockPlacementSample> samples, long slowThresholdMillis) {
        this.sampleCount = samples.size();
        int slow = 0;
        long fastest = Long.MAX_VALUE;
        long slowest = 0L;
        double total = 0.0D;

        for (BlockPlacementSample sample : samples) {
            long interval = sample.getIntervalMillis();
            total += interval;
            fastest = Math.min(fastest, interval);
            slowest = Math.max(slowest, interval);
            if (interval >= slowThresholdMillis) {
                slow++;
            }
        }

        this.slowPlacementCount = slow;
        this.averageIntervalMillis = samples.isEmpty() ? 0.0D : total / samples.size();
        this.fastestIntervalMillis = samples.isEmpty() ? 0L : fastest;
        this.slowestIntervalMillis = slowest;

        double squaredDifferenceTotal = 0.0D;
        for (BlockPlacementSample sample : samples) {
            double difference = sample.getIntervalMillis() - averageIntervalMillis;
            squaredDifferenceTotal += difference * difference;
        }
        this.standardDeviationMillis = samples.isEmpty()
                ? 0.0D : Math.sqrt(squaredDifferenceTotal / samples.size());
    }

    public int getSampleCount() { return sampleCount; }
    public int getSlowPlacementCount() { return slowPlacementCount; }
    public double getAverageIntervalMillis() { return averageIntervalMillis; }
    public long getFastestIntervalMillis() { return fastestIntervalMillis; }
    public long getSlowestIntervalMillis() { return slowestIntervalMillis; }
    public double getStandardDeviationMillis() { return standardDeviationMillis; }
}
