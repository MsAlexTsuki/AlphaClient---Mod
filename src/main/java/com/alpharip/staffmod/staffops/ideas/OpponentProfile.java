package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Stores prior encounters and notes by opponent. */
public class OpponentProfile {
 public static final class Encounter {public final long timestamp;public final boolean won;public final String note;public Encounter(long t,boolean w,String n){if(t<0)throw new IllegalArgumentException();timestamp=t;won=w;note=n==null?"":n;}}
 private final Map<String,List<Encounter>> data=new LinkedHashMap<String,List<Encounter>>();
 public void add(String name,Encounter e){String k=key(name);if(e==null)throw new IllegalArgumentException("encounter");if(!data.containsKey(k))data.put(k,new ArrayList<Encounter>());data.get(k).add(e);}
 public List<Encounter> get(String name){List<Encounter> v=data.get(key(name));return v==null?Collections.<Encounter>emptyList():Collections.unmodifiableList(new ArrayList<Encounter>(v));}
 public int wins(String name){int n=0;for(Encounter e:get(name))if(e.won)n++;return n;}public int losses(String name){return get(name).size()-wins(name);}
 private static String key(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("opponent");return s.trim().toLowerCase(Locale.ROOT);}
}