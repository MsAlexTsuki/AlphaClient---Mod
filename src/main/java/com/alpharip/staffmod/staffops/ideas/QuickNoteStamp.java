package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Timestamped staff notes. */
public final class QuickNoteStamp {
 public static final String NAME="Quick Note Stamp",CATEGORY="StaffMods",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 public static final class Note {public final String text,location,author;public final long createdAt;private Note(String t,String l,String a,long time){text=t;location=l;author=a;createdAt=time;}}
 private final List<Note> notes=new ArrayList<Note>();
 public synchronized Note add(String text,String location,String author,long timestamp){if(timestamp<0)throw new IllegalArgumentException("timestamp must be non-negative");Note n=new Note(req(text),location==null?"":location.trim(),author==null?"":author.trim(),timestamp);notes.add(n);return n;}
 public synchronized List<Note> snapshot(){return Collections.unmodifiableList(new ArrayList<Note>(notes));}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("note required");return s.trim();}
}