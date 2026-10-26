package com.alpharip.staffmod.staffops.ideas;

import java.util.*;

/** Team Roster Overview. Maintains a team-oriented roster overview from locally supplied entries. Foundation only; Minecraft/Forge GUI and runtime integration remain separate. */
public final class TeamRosterOverview {
 public static final String NAME="Team Roster Overview", CATEGORY="Management", STATUS="NEW";
 public static final String PURPOSE="Maintains a team-oriented roster overview from locally supplied entries.", REQUIRED_ACCESS="Management";
 public static final boolean REQUIRES_SUPABASE=false, REQUIRES_SERVER_SUPPORT=false;
 private final Map<String,Map<String,String>> entries=new LinkedHashMap<String,Map<String,String>>();
 public synchronized void put(String id,Map<String,String> fields){String k=text(id,"id");if(fields==null)throw new IllegalArgumentException("fields must not be null");entries.put(k,Collections.unmodifiableMap(new LinkedHashMap<String,String>(fields)));}
 public synchronized Map<String,String> get(String id){return entries.get(text(id,"id"));}
 public synchronized boolean remove(String id){return entries.remove(text(id,"id"))!=null;}
 public synchronized Map<String,Map<String,String>> snapshot(){return Collections.unmodifiableMap(new LinkedHashMap<String,Map<String,String>>(entries));}
 private static String text(String v,String n){if(v==null||v.trim().isEmpty())throw new IllegalArgumentException(n+" must not be blank");return v.trim();}
}
