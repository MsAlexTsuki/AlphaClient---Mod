package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Local notes attached to an evidence review. */
public final class EvidenceReviewNotes {
 private final List<Note> notes=new ArrayList<Note>();
 public void add(String authorId,String text,long createdAtMillis){if(text==null||text.trim().isEmpty())throw new IllegalArgumentException("Review note must not be blank");notes.add(new Note(authorId,text.trim(),createdAtMillis));}
 public List<Note> getNotes(){return Collections.unmodifiableList(new ArrayList<Note>(notes));}
 public static final class Note {private final String authorId,text;private final long createdAtMillis;private Note(String authorId,String text,long createdAtMillis){this.authorId=authorId==null?"":authorId;this.text=text;this.createdAtMillis=createdAtMillis;}public String getAuthorId(){return authorId;}public String getText(){return text;}public long getCreatedAtMillis(){return createdAtMillis;}}
}