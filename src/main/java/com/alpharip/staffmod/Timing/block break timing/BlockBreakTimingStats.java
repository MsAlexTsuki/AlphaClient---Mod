package com.alpharip.staffmod.timing.blockbreak;

import java.util.List;

/** Immutable aggregate statistics for a block-break timing sample set. */
public final class BlockBreakTimingStats {
    private final int sampleCount;
    private final int slowCount;
    private final double averageMillis;
    private final long fastestMillis;
    private final long slowestMillis;
    private final double standardDeviationMillis;

    public BlockBreakTimingStats(List<BlockBreakSample> samples, long slowThresholdMillis) {
        if (samples == null) throw new IllegalArgumentException("samples cannot be null");
        int count = samples.size();
        int slow = 0;
        double sum = 0.0D;
        long fastest = Long.MAX_VALUE;
        long slowest = 0L;
        for (BlockBreakSample sample : samples) {
            long value = sample.getIntervalMillis();
            sum += value;
            if (value > slowThresholdMillis) slow++;
            if (value < fastest) fastest = value;
            if (value > slowest) slowest = value;
        }
        this.sampleCount = count;
        this.slowCount = slow;
        this.averageMillis = count == 0 ? 0.0D : sum / count;
        this.fastestMillis = count == 0 ? 0L : fastest;
        this.slowestMillis = count == 0 ? 0L : slowest;

        double squaredDifference = 0.0D;
        for (BlockBreakSample sample : samples) {
            double difference = sample.getIntervalMillis() - averageMillis;
            squaredDifference += difference * difference;
        }
        this.standardDeviationMillis = count == 0 ? 0.0D : Math.sqrt(squaredDifference / count);
    }

    public int getSampleCount() { return sampleCount; }
    public int getSlowCount() { return slowCount; }
    public double getAverageMillis() { return averageMillis; }
    public long getFastestMillis() { return fastestMillis; }
    public long getSlowestMillis() { return slowestMillis; }
    public double getStandardDeviationMillis() { return standardDeviationMillis; }
}
