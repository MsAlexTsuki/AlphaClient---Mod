package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Local collection of earned cosmetic trophies. */
public final class TrophyShelf {
 public static final String NAME="Trophy Shelf",CATEGORY="OwnerFun",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private final Map<String,String> trophies=new LinkedHashMap<String,String>();
 public synchronized boolean unlock(String id,String title){String k=req(id);if(trophies.containsKey(k))return false;trophies.put(k,req(title));return true;}
 public synchronized boolean remove(String id){return trophies.remove(req(id))!=null;}
 public synchronized List<String> titles(){return Collections.unmodifiableList(new ArrayList<String>(trophies.values()));}
 public synchronized boolean has(String id){return trophies.containsKey(req(id));}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}