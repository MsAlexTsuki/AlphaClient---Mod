package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** PriorHistoryLookupPanel. Shows authorised prior history alongside a current case when a supported data source is available. Foundation only; Forge GUI/event integration is not included yet. */
public final class PriorHistoryLookupPanel {
 public static final String NAME="Prior History Lookup Panel", CATEGORY="Staff", STATUS="NEEDS VERIFICATION";
 public static final String PURPOSE="Shows authorised prior history alongside a current case when a supported data source is available.";
 public static final boolean REQUIRES_SUPABASE=true, REQUIRES_SERVER_SUPPORT=true;
 TODO(Supabase): connect the authorised history source after owner configuration.
 // TODO(Server): server/API must enforce access to private player history.
 private final Map<String,Map<String,String>> records=new LinkedHashMap<String,Map<String,String>>();
 public synchronized void put(String id,Map<String,String> fields){if(id==null||id.trim().isEmpty()||fields==null)throw new IllegalArgumentException("id and fields required");records.put(id.trim(),Collections.unmodifiableMap(new LinkedHashMap<String,String>(fields)));}
 public synchronized Map<String,String> get(String id){return records.get(id);}
 public synchronized boolean remove(String id){return records.remove(id)!=null;}
 public synchronized Map<String,Map<String,String>> snapshot(){return Collections.unmodifiableMap(new LinkedHashMap<String,Map<String,String>>(records));}
}