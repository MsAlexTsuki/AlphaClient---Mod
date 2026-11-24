package com.alpharip.staffmod;

/** Converts a confirmed pet-death observation into a memorial record. */
public final class PetDeathRecorder {
    private final PetMemorialBook book;

    public PetDeathRecorder(PetMemorialBook book) {
        if (book == null) throw new IllegalArgumentException("book must not be null");
        this.book = book;
    }

    public PetMemorialRecord recordDeath(String petName, String species, String owner,
                                         String world, String cause, long timestamp) {
        PetMemorialRecord record = new PetMemorialRecord(
                petName, species, owner, world, cause, timestamp);
        book.add(record);
        return record;
    }
}
