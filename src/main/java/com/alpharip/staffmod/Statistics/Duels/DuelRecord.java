package com.alpharip.staffmod.statistics.duels;

/** Immutable record of one completed duel. */
public final class DuelRecord {
    private final boolean win;
    private final long timestamp;

    public DuelRecord(boolean win, long timestamp) {
        this.win = win;
        this.timestamp = timestamp;
    }

    public boolean isWin() { return win; }
    public boolean isLoss() { return !win; }
    public long getTimestamp() { return timestamp; }
}
