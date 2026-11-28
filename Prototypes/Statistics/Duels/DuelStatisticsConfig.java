package com.alpharip.staffmod.statistics.duels;

/** Immutable settings for per-opponent duel history. */
public final class DuelStatisticsConfig {
    private final boolean historyEnabled;
    private final int maxHistoryEntries;

    public DuelStatisticsConfig(boolean historyEnabled, int maxHistoryEntries) {
        if (maxHistoryEntries < 1) throw new IllegalArgumentException("maxHistoryEntries must be positive");
        this.historyEnabled = historyEnabled;
        this.maxHistoryEntries = maxHistoryEntries;
    }

    public static DuelStatisticsConfig defaults() {
        return new DuelStatisticsConfig(true, 100);
    }

    public boolean isHistoryEnabled() { return historyEnabled; }
    public int getMaxHistoryEntries() { return maxHistoryEntries; }
}
