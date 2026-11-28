package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** ContextProfileSwitcher. Switches local profiles when configured server-mode or world-context rules match. Local foundation only; Forge event/UI integration is pending. */
public final class ContextProfileSwitcher {
 public static final String NAME="Context Profile Switcher", CATEGORY="Cross-Category", STATUS="NEW";
 public static final String PURPOSE="Switches local profiles when configured server-mode or world-context rules match."; public static final boolean REQUIRES_SUPABASE=false, REQUIRES_SERVER_SUPPORT=false;
 
 private final Map<String,Map<String,String>> records=new LinkedHashMap<String,Map<String,String>>();
 public synchronized void put(String id,Map<String,String> fields){if(id==null||id.trim().isEmpty()||fields==null)throw new IllegalArgumentException("id and fields required");records.put(id.trim(),Collections.unmodifiableMap(new LinkedHashMap<String,String>(fields)));}
 public synchronized Map<String,String> get(String id){return records.get(id);}
 public synchronized boolean remove(String id){return records.remove(id)!=null;}
 public synchronized Map<String,Map<String,String>> snapshot(){return Collections.unmodifiableMap(new LinkedHashMap<String,Map<String,String>>(records));}
}