package com.alpharip.staffmod;

/** Receives observations from a future client-world/entity scanner. */
public final class BestiaryTracker {
    private final BestiaryBook book;

    public BestiaryTracker(BestiaryBook book) {
        if (book == null) throw new IllegalArgumentException("book must not be null");
        this.book = book;
    }

    public BestiaryEntry onMobEncounter(String registryId, String displayName, String worldName, long timestamp) {
        return book.record(registryId, displayName, worldName, timestamp);
    }

    public BestiaryBook getBook() { return book; }
}
