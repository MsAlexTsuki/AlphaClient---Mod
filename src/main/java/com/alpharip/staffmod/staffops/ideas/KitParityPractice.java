package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** KitParityPractice. Maps the saved KitMap hotbar layout into practice drills; the practice server must provide matching kit items. Local foundation only; Forge event/UI integration is pending. */
public final class KitParityPractice {
 public static final String NAME="Kit Parity Practice", CATEGORY="Cross-Category", STATUS="NEW";
 public static final String PURPOSE="Maps the saved KitMap hotbar layout into practice drills; the practice server must provide matching kit items."; public static final boolean REQUIRES_SUPABASE=false, REQUIRES_SERVER_SUPPORT=true;
 // TODO(Server): practice server must provide the kit items; the client only maps local hotbar positions.
 private final Map<String,Map<String,String>> records=new LinkedHashMap<String,Map<String,String>>();
 public synchronized void put(String id,Map<String,String> fields){if(id==null||id.trim().isEmpty()||fields==null)throw new IllegalArgumentException("id and fields required");records.put(id.trim(),Collections.unmodifiableMap(new LinkedHashMap<String,String>(fields)));}
 public synchronized Map<String,String> get(String id){return records.get(id);}
 public synchronized boolean remove(String id){return records.remove(id)!=null;}
 public synchronized Map<String,Map<String,String>> snapshot(){return Collections.unmodifiableMap(new LinkedHashMap<String,Map<String,String>>(records));}
}