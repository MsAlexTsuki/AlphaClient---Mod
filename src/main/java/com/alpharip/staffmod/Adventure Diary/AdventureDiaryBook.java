package com.alpharip.staffmod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores narrated events in chronological insertion order. */
public final class AdventureDiaryBook {
    private final DramaticDiaryNarrator narrator;
    private final List<AdventureDiaryEntry> entries = new ArrayList<AdventureDiaryEntry>();

    public AdventureDiaryBook(DramaticDiaryNarrator narrator) {
        if (narrator == null) throw new IllegalArgumentException("narrator must not be null");
        this.narrator = narrator;
    }

    public synchronized AdventureDiaryEntry record(AdventureDiaryEvent event) {
        AdventureDiaryEntry entry = new AdventureDiaryEntry(event, narrator.narrate(event));
        entries.add(entry);
        return entry;
    }

    public synchronized List<AdventureDiaryEntry> getEntries() {
        return Collections.unmodifiableList(new ArrayList<AdventureDiaryEntry>(entries));
    }

    public synchronized void clear() { entries.clear(); }
}
