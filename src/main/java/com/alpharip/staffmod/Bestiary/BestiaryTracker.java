package com.alpharip.staffmod.Bestiary;

/** Accepts confirmed player kills; sightings and encounters never unlock entries. */
public final class BestiaryTracker {
    private final BestiaryBook book;

    public BestiaryTracker(BestiaryBook book) {
        if (book == null) throw new IllegalArgumentException("book must not be null");
        this.book = book;
    }

    public BestiaryEntry onMobKilled(String registryId, String displayName, String worldName, long timestamp) {
        return book.recordKill(registryId, displayName, worldName, timestamp);
    }

    public BestiaryBook getBook() { return book; }
}
