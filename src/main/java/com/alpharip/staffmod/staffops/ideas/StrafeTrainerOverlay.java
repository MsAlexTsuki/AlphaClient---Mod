package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/**
 * Stores strafe-training settings and samples.
 * Client-side model only: actual Minecraft UI/event hooks and persistence are not connected.
 * Add TODO(Supabase) integration only where shared cross-session data is required.
 * Compatible with Java 8.
 */
public class StrafeTrainerOverlay {
 private boolean enabled = true;
 private final LinkedHashMap<String,String> entries = new LinkedHashMap<String,String>();
 public boolean isEnabled(){return enabled;}
 public void setEnabled(boolean value){enabled=value;}
 public void put(String key,String value){entries.put(require(key),value==null?"":value);}
 public String get(String key){return entries.get(require(key));}
 public boolean remove(String key){return entries.remove(require(key))!=null;}
 public Map<String,String> snapshot(){return Collections.unmodifiableMap(new LinkedHashMap<String,String>(entries));}
 public List<String> search(String query){String q=require(query).toLowerCase(Locale.ROOT);List<String> out=new ArrayList<String>();for(Map.Entry<String,String> e:entries.entrySet())if(e.getKey().toLowerCase(Locale.ROOT).contains(q)||e.getValue().toLowerCase(Locale.ROOT).contains(q))out.add(e.getKey());return Collections.unmodifiableList(out);}
 private static String require(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("Value must not be blank.");return s.trim();}
}