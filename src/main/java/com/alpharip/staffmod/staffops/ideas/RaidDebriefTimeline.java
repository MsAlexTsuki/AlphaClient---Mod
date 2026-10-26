package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** RaidDebriefTimeline. Combines local event logs and replay markers into a raid-level timeline. Local foundation only; Forge event/UI integration is pending. */
public final class RaidDebriefTimeline {
 public static final String NAME="Raid Debrief Timeline", CATEGORY="Cross-Category", STATUS="NEW";
 public static final String PURPOSE="Combines local event logs and replay markers into a raid-level timeline."; public static final boolean REQUIRES_SUPABASE=false, REQUIRES_SERVER_SUPPORT=false;
 
 private final Map<String,Map<String,String>> records=new LinkedHashMap<String,Map<String,String>>();
 public synchronized void put(String id,Map<String,String> fields){if(id==null||id.trim().isEmpty()||fields==null)throw new IllegalArgumentException("id and fields required");records.put(id.trim(),Collections.unmodifiableMap(new LinkedHashMap<String,String>(fields)));}
 public synchronized Map<String,String> get(String id){return records.get(id);}
 public synchronized boolean remove(String id){return records.remove(id)!=null;}
 public synchronized Map<String,Map<String,String>> snapshot(){return Collections.unmodifiableMap(new LinkedHashMap<String,Map<String,String>>(records));}
}