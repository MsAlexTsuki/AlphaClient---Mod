package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Local note store keyed by spar-partner identifier. Persistence/UI integration remains separate. */
public final class SparPartnerNotes {
 public static final String NAME="Spar Partner Notes",CATEGORY="Practice",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private final Map<String,List<String>> notes=new LinkedHashMap<String,List<String>>();
 public synchronized void add(String partner,String note){String p=text(partner),n=text(note);notes.computeIfAbsent(p,k->new ArrayList<String>()).add(n);}
 public synchronized List<String> get(String partner){List<String> n=notes.get(text(partner));return Collections.unmodifiableList(n==null?new ArrayList<String>():new ArrayList<String>(n));}
 public synchronized boolean remove(String partner,int index){List<String> n=notes.get(text(partner));if(n==null||index<0||index>=n.size())return false;n.remove(index);if(n.isEmpty())notes.remove(text(partner));return true;}
 private static String text(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}