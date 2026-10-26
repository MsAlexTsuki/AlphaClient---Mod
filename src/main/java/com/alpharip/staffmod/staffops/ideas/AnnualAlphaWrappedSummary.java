package com.alpharip.staffmod.staffops.ideas;

/**
 * Foundation for an annual Alpha usage summary.
 * TODO(Supabase): retrieve yearly activity aggregates with appropriate user privacy controls.
 * Summary generation, presentation, opt-in handling, and tests remain to be implemented.
 */
public class AnnualAlphaWrappedSummary {
    private int year;
    private long totalPlaytimeMillis;
    private long sessions;
    private long serversVisited;
    private boolean available;

    public AnnualAlphaWrappedSummary(int year) { this.year = year; }
    public int getYear() { return year; }
    public void setYear(int value) { year = value; }
    public long getTotalPlaytimeMillis() { return totalPlaytimeMillis; }
    public void setTotalPlaytimeMillis(long value) { totalPlaytimeMillis = Math.max(0L, value); }
    public long getSessions() { return sessions; }
    public void setSessions(long value) { sessions = Math.max(0L, value); }
    public long getServersVisited() { return serversVisited; }
    public void setServersVisited(long value) { serversVisited = Math.max(0L, value); }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean value) { available = value; }
}