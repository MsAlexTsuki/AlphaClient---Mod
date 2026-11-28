package com.alpharip.staffmod.trainers.bridge;

/** Immutable settings for the local Bridge Speed Trainer prototype. */
public final class BridgeTrainerConfig {
    private final int maximumHistoryEntries;

    public BridgeTrainerConfig() {
        this(10);
    }

    public BridgeTrainerConfig(int maximumHistoryEntries) {
        if (maximumHistoryEntries < 1) {
            throw new IllegalArgumentException("maximumHistoryEntries must be at least 1.");
        }
        this.maximumHistoryEntries = maximumHistoryEntries;
    }

    public int getMaximumHistoryEntries() {
        return maximumHistoryEntries;
    }
}
