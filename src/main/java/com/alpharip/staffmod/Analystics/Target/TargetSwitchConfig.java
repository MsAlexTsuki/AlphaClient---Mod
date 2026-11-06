package com.alpharip.staffmod.analystics.target;

/** Immutable local configuration for target-switch history. */
public final class TargetSwitchConfig {
    private final int maxEvents;

    public TargetSwitchConfig() { this(500); }

    public TargetSwitchConfig(int maxEvents) {
        if (maxEvents < 1) throw new IllegalArgumentException("maxEvents must be at least 1");
        this.maxEvents = maxEvents;
    }

    public int getMaxEvents() { return maxEvents; }
}
