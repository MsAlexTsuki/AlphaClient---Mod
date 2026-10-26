package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Stores notes by world identifier. Local persistence is not connected. */
public class WorldNotes {private final Map<String,List<String>> notes=new LinkedHashMap<String,List<String>>();
 public void add(String world,String note){String w=req(world),n=req(note);if(!notes.containsKey(w))notes.put(w,new ArrayList<String>());notes.get(w).add(n);}
 public List<String> get(String world){List<String> v=notes.get(req(world));return v==null?Collections.<String>emptyList():Collections.unmodifiableList(new ArrayList<String>(v));}
 public boolean remove(String world,int index){List<String> v=notes.get(req(world));if(v==null||index<0||index>=v.size())return false;v.remove(index);if(v.isEmpty())notes.remove(req(world));return true;}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException();return s.trim();}
}