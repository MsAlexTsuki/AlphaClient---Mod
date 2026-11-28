package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores local collaboration notes; it does not synchronize notes between users. */
public final class InvestigationCollaborationNotes {
    public static final class Note {
        private final String author;
        private final long timestampMillis;
        private final String text;

        public Note(String author, long timestampMillis, String text) {
            this.author = author == null ? "" : author.trim();
            this.timestampMillis = timestampMillis;
            this.text = text == null ? "" : text.trim();
        }
        public String getAuthor() { return author; }
        public long getTimestampMillis() { return timestampMillis; }
        public String getText() { return text; }
    }

    private final List<Note> notes = new ArrayList<Note>();
    public synchronized void add(Note note) { if (note != null) notes.add(note); }
    public synchronized List<Note> getNotes() {
        return Collections.unmodifiableList(new ArrayList<Note>(notes));
    }
}