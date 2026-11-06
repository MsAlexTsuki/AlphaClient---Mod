package com.alpharip.staffmod.AfkCamera;

/**
 * Tracks the most recent activity timestamp supplied by a future input/event adapter.
 * This class does not decide which Minecraft inputs count as activity.
 */
public final class AfkActivityTracker {
    private long lastActivityMillis = -1L;

    public synchronized void recordActivity(long timestampMillis) {
        if (timestampMillis < 0L) throw new IllegalArgumentException("timestampMillis cannot be negative");
        lastActivityMillis = timestampMillis;
    }

    public synchronized boolean hasActivity() {
        return lastActivityMillis >= 0L;
    }

    public synchronized long getLastActivityMillis() {
        return lastActivityMillis;
    }

    public synchronized long getIdleDurationMillis(long nowMillis) {
        if (lastActivityMillis < 0L || nowMillis <= lastActivityMillis) return 0L;
        return nowMillis - lastActivityMillis;
    }

    public synchronized void reset() {
        lastActivityMillis = -1L;
    }
}
