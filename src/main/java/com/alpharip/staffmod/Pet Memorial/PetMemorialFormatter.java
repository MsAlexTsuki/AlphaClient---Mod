package com.alpharip.staffmod;

/** Produces a compact, respectful memorial inscription. */
public final class PetMemorialFormatter {
    public String format(PetMemorialRecord record) {
        if (record == null) throw new IllegalArgumentException("record must not be null");
        return record.getPetName() + " — " + record.getSpecies()
                + "\nBeloved companion of " + record.getOwnerName()
                + "\nRemembered in " + record.getWorldName()
                + "\nCause: " + record.getCause();
    }
}
