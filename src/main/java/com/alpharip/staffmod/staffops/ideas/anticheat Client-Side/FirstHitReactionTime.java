package com.alpharip.staffmod.staffops.ideas;

/**
 * Stores elapsed time between a combat observation starting and the first hit.
 * Detection of the observation and hit events must be wired into the client.
 */
public final class FirstHitReactionTime {
    private long observationStartMillis = -1L;
    private long firstHitReactionMillis = -1L;

    public void startObservation(long timestampMillis) {
        observationStartMillis = timestampMillis;
        firstHitReactionMillis = -1L;
    }

    public boolean recordFirstHit(long timestampMillis) {
        if (observationStartMillis < 0L || firstHitReactionMillis >= 0L
                || timestampMillis < observationStartMillis) {
            return false;
        }
        firstHitReactionMillis = timestampMillis - observationStartMillis;
        return true;
    }

    public long getFirstHitReactionMillis() {
        return firstHitReactionMillis;
    }

    public boolean hasFirstHit() {
        return firstHitReactionMillis >= 0L;
    }

    public void reset() {
        observationStartMillis = -1L;
        firstHitReactionMillis = -1L;
    }
}
