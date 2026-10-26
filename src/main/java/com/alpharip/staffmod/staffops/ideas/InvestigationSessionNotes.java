package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Timestamped notes kept locally for an investigation session. */
public final class InvestigationSessionNotes {
    public static final class Note {
        private final long timestampMillis;
        private final String text;
        public Note(long timestampMillis, String text) {
            this.timestampMillis = timestampMillis;
            this.text = text == null ? "" : text;
        }
        public long getTimestampMillis() { return timestampMillis; }
        public String getText() { return text; }
    }

    private final List<Note> notes = new ArrayList<Note>();

    public synchronized void add(long timestampMillis, String text) {
        if (text == null || text.trim().isEmpty()) return;
        notes.add(new Note(timestampMillis, text.trim()));
    }
    public synchronized List<Note> getNotes() {
        return Collections.unmodifiableList(new ArrayList<Note>(notes));
    }
    public synchronized void clear() { notes.clear(); }
}