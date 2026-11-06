package com.alpharip.staffmod.wrapped;

/** Immutable result for one calendar year's local activity summary. */
public final class AnnualAlphaWrappedSummary {
    private final int year;
    private final long totalPlaytimeMillis;
    private final long sessions;
    private final long serversVisited;
    private final boolean available;

    public AnnualAlphaWrappedSummary(int year, long totalPlaytimeMillis, long sessions,
                                     long serversVisited, boolean available) {
        this.year = year;
        this.totalPlaytimeMillis = Math.max(0L, totalPlaytimeMillis);
        this.sessions = Math.max(0L, sessions);
        this.serversVisited = Math.max(0L, serversVisited);
        this.available = available;
    }

    public int getYear() { return year; }
    public long getTotalPlaytimeMillis() { return totalPlaytimeMillis; }
    public long getSessions() { return sessions; }
    public long getServersVisited() { return serversVisited; }
    public boolean isAvailable() { return available; }
}
