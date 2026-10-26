package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** StaffDutyMode. Switches local HUD/profile presentation between play mode and staff duty mode. Local foundation only; Forge event/UI integration is pending. */
public final class StaffDutyMode {
 public static final String NAME="Staff Duty Mode", CATEGORY="Cross-Category", STATUS="NEW";
 public static final String PURPOSE="Switches local HUD/profile presentation between play mode and staff duty mode."; public static final boolean REQUIRES_SUPABASE=true, REQUIRES_SERVER_SUPPORT=true;
 TODO(Supabase): optional duty audit sync later.
 // TODO(Server): actual staff duty status must be server-confirmed.
 private final Map<String,Map<String,String>> records=new LinkedHashMap<String,Map<String,String>>();
 public synchronized void put(String id,Map<String,String> fields){if(id==null||id.trim().isEmpty()||fields==null)throw new IllegalArgumentException("id and fields required");records.put(id.trim(),Collections.unmodifiableMap(new LinkedHashMap<String,String>(fields)));}
 public synchronized Map<String,String> get(String id){return records.get(id);}
 public synchronized boolean remove(String id){return records.remove(id)!=null;}
 public synchronized Map<String,Map<String,String>> snapshot(){return Collections.unmodifiableMap(new LinkedHashMap<String,Map<String,String>>(records));}
}