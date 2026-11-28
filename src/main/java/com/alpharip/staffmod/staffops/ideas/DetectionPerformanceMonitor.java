package com.alpharip.staffmod.staffops.ideas;

/** Tracks caller-supplied operation durations without instrumenting them automatically. */
public class DetectionPerformanceMonitor {
    private long sampleCount;
    private long totalNanos;
    private long maximumNanos;

    public void recordDuration(long nanos) {
        if (nanos < 0L) { return; }
        sampleCount++;
        totalNanos += nanos;
        maximumNanos = Math.max(maximumNanos, nanos);
    }
    public long getSampleCount() { return sampleCount; }
    public long getTotalNanos() { return totalNanos; }
    public long getMaximumNanos() { return maximumNanos; }
    public double getAverageNanos() {
        return sampleCount == 0L ? 0.0D : (double) totalNanos / sampleCount;
    }
    public void reset() { sampleCount = 0L; totalNanos = 0L; maximumNanos = 0L; }
}
