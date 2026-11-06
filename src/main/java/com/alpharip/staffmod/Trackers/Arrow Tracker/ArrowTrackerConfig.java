package com.alpharip.staffmod.trackers.arrow;

/** Immutable limits for the Arrow Tracker prototype. */
public final class ArrowTrackerConfig {
    private final int maximumTrackedArrows;

    public ArrowTrackerConfig() {
        this(128);
    }

    public ArrowTrackerConfig(int maximumTrackedArrows) {
        if (maximumTrackedArrows < 1) {
            throw new IllegalArgumentException("maximumTrackedArrows must be at least 1");
        }
        this.maximumTrackedArrows = maximumTrackedArrows;
    }

    public int getMaximumTrackedArrows() {
        return maximumTrackedArrows;
    }
}
