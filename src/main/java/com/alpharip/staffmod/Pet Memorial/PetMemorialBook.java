package com.alpharip.staffmod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores memorial records without deleting earlier companions. */
public final class PetMemorialBook {
    private final List<PetMemorialRecord> records = new ArrayList<PetMemorialRecord>();

    public synchronized void add(PetMemorialRecord record) {
        if (record == null) throw new IllegalArgumentException("record must not be null");
        records.add(record);
    }

    public synchronized List<PetMemorialRecord> getRecords() {
        return Collections.unmodifiableList(new ArrayList<PetMemorialRecord>(records));
    }

    public synchronized int size() { return records.size(); }
}
