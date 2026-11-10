package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Local notes keyed by allied faction name. */
public final class AllyFactionNotes {
 public static final String NAME="Ally Faction Notes",CATEGORY="HardcoreFactions",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private final Map<String,List<String>> notes=new LinkedHashMap<String,List<String>>();
 public synchronized void add(String faction,String note){String f=text(faction),n=text(note);notes.computeIfAbsent(f,k->new ArrayList<String>()).add(n);}
 public synchronized List<String> get(String faction){List<String> n=notes.get(text(faction));return Collections.unmodifiableList(n==null?new ArrayList<String>():new ArrayList<String>(n));}
 public synchronized void clear(String faction){notes.remove(text(faction));}
 private static String text(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}