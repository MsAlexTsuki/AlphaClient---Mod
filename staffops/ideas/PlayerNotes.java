package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Local notes keyed by normalized player name. Shared persistence is not connected. */
public class PlayerNotes {private final Map<String,List<String>> notes=new LinkedHashMap<String,List<String>>();
 public void add(String player,String note){String p=req(player).toLowerCase(Locale.ROOT),n=req(note);if(!notes.containsKey(p))notes.put(p,new ArrayList<String>());notes.get(p).add(n);}
 public List<String> get(String player){List<String> v=notes.get(req(player).toLowerCase(Locale.ROOT));return v==null?Collections.<String>emptyList():Collections.unmodifiableList(new ArrayList<String>(v));}
 public void clear(String player){notes.remove(req(player).toLowerCase(Locale.ROOT));}private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException();return s.trim();}
}