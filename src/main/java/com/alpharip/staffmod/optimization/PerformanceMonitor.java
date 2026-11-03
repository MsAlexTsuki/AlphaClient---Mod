// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.optimization;

import net.minecraft.client.Minecraft;

/**
 * Stores a small local snapshot of client performance.
 */
public final class PerformanceMonitor {
    private int latestFps;
    private long usedHeapBytes;
    private long maxHeapBytes;
    private long lastSampleTime;
    private long latestCoreUpdateNanos;
    private long maxCoreUpdateNanos;
    private long sampleCount;

    public synchronized void sample() {
        Runtime runtime = Runtime.getRuntime();
        latestFps = Minecraft.getDebugFPS();
        usedHeapBytes = runtime.totalMemory() - runtime.freeMemory();
        maxHeapBytes = runtime.maxMemory();
        lastSampleTime = System.currentTimeMillis();
        sampleCount++;
    }

    public synchronized void recordCoreUpdateDuration(long durationNanos) {
        latestCoreUpdateNanos = Math.max(0L, durationNanos);
        if (latestCoreUpdateNanos > maxCoreUpdateNanos) {
            maxCoreUpdateNanos = latestCoreUpdateNanos;
        }
    }

    public synchronized int getLatestFps() {
        return latestFps;
    }

    public synchronized long getUsedHeapBytes() {
        return usedHeapBytes;
    }

    public synchronized long getMaxHeapBytes() {
        return maxHeapBytes;
    }

    public synchronized long getLastSampleTime() {
        return lastSampleTime;
    }

    public synchronized long getLatestCoreUpdateNanos() {
        return latestCoreUpdateNanos;
    }

    public synchronized long getMaxCoreUpdateNanos() {
        return maxCoreUpdateNanos;
    }

    public synchronized long getSampleCount() {
        return sampleCount;
    }

    public synchronized String createReport() {
        return "FPS: " + latestFps + "\n"
                + "Heap used: " + (usedHeapBytes / 1048576L) + " MiB / "
                + (maxHeapBytes / 1048576L) + " MiB\n"
                + "Latest Core update: " + (latestCoreUpdateNanos / 1000000.0D) + " ms\n"
                + "Maximum Core update: " + (maxCoreUpdateNanos / 1000000.0D) + " ms\n"
                + "Performance samples: " + sampleCount;
    }
}
