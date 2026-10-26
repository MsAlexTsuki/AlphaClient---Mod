package com.alpharip.staffmod.staffops.ideas;

/**
 * Local countdown model for displaying a freeze duration.
 * It does not apply or remove a server-side freeze.
 */
public final class FreezeTimer {
    private long deadlineMillis;
    private boolean running;

    public void start(long nowMillis, long durationMillis) {
        if (durationMillis < 0L) {
            throw new IllegalArgumentException("durationMillis must be non-negative");
        }
        deadlineMillis = nowMillis + durationMillis;
        running = true;
    }

    public void stop() {
        running = false;
        deadlineMillis = 0L;
    }

    public long getRemainingMillis(long nowMillis) {
        if (!running) {
            return 0L;
        }
        long remaining = deadlineMillis - nowMillis;
        if (remaining <= 0L) {
            stop();
            return 0L;
        }
        return remaining;
    }

    public boolean isRunning() { return running; }
}