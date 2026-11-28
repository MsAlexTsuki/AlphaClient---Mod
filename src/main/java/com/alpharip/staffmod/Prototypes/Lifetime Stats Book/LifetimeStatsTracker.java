package com.alpharip.staffmod;

/** Receives normalized stat increments from future Minecraft event adapters. */
public final class LifetimeStatsTracker {
    private final LifetimeStatsBook book;

    public LifetimeStatsTracker(LifetimeStatsBook book) {
        if (book == null) throw new IllegalArgumentException("book must not be null");
        this.book = book;
    }

    public void record(LifetimeStat stat, long amount) {
        book.add(stat, amount);
    }

    public LifetimeStatsSnapshot getSnapshot() { return book.snapshot(); }
}
