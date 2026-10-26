package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Local notes attached to an investigation workspace; not shared or remotely persisted. */
public final class InvestigationNotes {
    private final List<Note> notes = new ArrayList<Note>();

    public Note add(String author, String text, long createdAtMillis) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Note text must not be blank");
        }
        Note note = new Note(author, text.trim(), createdAtMillis);
        notes.add(note);
        return note;
    }

    public List<Note> getNotes() {
        return Collections.unmodifiableList(new ArrayList<Note>(notes));
    }

    public static final class Note {
        private final String author;
        private final String text;
        private final long createdAtMillis;

        public Note(String author, String text, long createdAtMillis) {
            this.author = author == null ? "" : author;
            this.text = text;
            this.createdAtMillis = createdAtMillis;
        }

        public String getAuthor() { return author; }
        public String getText() { return text; }
        public long getCreatedAtMillis() { return createdAtMillis; }
    }

    // TODO(Supabase): define case access and audit requirements before shared note storage.
}