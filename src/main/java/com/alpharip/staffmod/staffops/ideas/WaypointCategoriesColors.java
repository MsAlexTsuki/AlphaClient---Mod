package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Maps waypoint names to categories and RGB colors. Actual Minecraft UI/event hooks and persistence are not connected. Compatible with Java 8. */
public class WaypointCategoriesColors {
 private boolean enabled=true;private final LinkedHashMap<String,String> entries=new LinkedHashMap<String,String>();
 public boolean isEnabled(){return enabled;}public void setEnabled(boolean v){enabled=v;}
 public void put(String key,String value){entries.put(req(key),value==null?"":value);}
 public String get(String key){return entries.get(req(key));}public boolean remove(String key){return entries.remove(req(key))!=null;}
 public Map<String,String> snapshot(){return Collections.unmodifiableMap(new LinkedHashMap<String,String>(entries));}
 public List<String> search(String q){String s=req(q).toLowerCase(Locale.ROOT);List<String> out=new ArrayList<String>();for(Map.Entry<String,String> e:entries.entrySet())if(e.getKey().toLowerCase(Locale.ROOT).contains(s)||e.getValue().toLowerCase(Locale.ROOT).contains(s))out.add(e.getKey());return Collections.unmodifiableList(out);}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("Value must not be blank.");return s.trim();}
}