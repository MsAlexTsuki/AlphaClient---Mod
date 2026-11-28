package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** PolicyEffectTracker. Tracks configured policy changes alongside manually entered outcome metrics. Local foundation only; Forge integration is pending. */
public final class PolicyEffectTracker {
 public static final String NAME="Policy Effect Tracker", CATEGORY="Senior Staff", STATUS="NEW";
 public static final String PURPOSE="Tracks configured policy changes alongside manually entered outcome metrics."; public static final boolean REQUIRES_SUPABASE=true, REQUIRES_SERVER_SUPPORT=false;
 TODO(Supabase): shared policy/outcome data is not connected yet.
 private final Map<String,Map<String,String>> records=new LinkedHashMap<String,Map<String,String>>();
 public synchronized void put(String id,Map<String,String> fields){if(id==null||id.trim().isEmpty()||fields==null)throw new IllegalArgumentException("id and fields required");records.put(id.trim(),Collections.unmodifiableMap(new LinkedHashMap<String,String>(fields)));}
 public synchronized Map<String,String> get(String id){return records.get(id);}
 public synchronized boolean remove(String id){return records.remove(id)!=null;}
 public synchronized Map<String,Map<String,String>> snapshot(){return Collections.unmodifiableMap(new LinkedHashMap<String,Map<String,String>>(records));}
}