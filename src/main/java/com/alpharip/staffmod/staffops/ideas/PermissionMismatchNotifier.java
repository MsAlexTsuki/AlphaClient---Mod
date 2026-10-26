package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** PermissionMismatchNotifier. Compares locally visible permissions with an approved expected permission snapshot and warns on mismatch. Local foundation only; Forge event/UI integration is pending. */
public final class PermissionMismatchNotifier {
 public static final String NAME="Permission Mismatch Notifier", CATEGORY="Security", STATUS="NEW";
 public static final String PURPOSE="Compares locally visible permissions with an approved expected permission snapshot and warns on mismatch."; public static final boolean REQUIRES_SUPABASE=true, REQUIRES_SERVER_SUPPORT=true;
 TODO(Supabase): retrieve the trusted permission snapshot later.
 // TODO(Server): this warning is not a permission grant; server checks remain authoritative.
 private final Map<String,Map<String,String>> records=new LinkedHashMap<String,Map<String,String>>();
 public synchronized void put(String id,Map<String,String> fields){if(id==null||id.trim().isEmpty()||fields==null)throw new IllegalArgumentException("id and fields required");records.put(id.trim(),Collections.unmodifiableMap(new LinkedHashMap<String,String>(fields)));}
 public synchronized Map<String,String> get(String id){return records.get(id);}
 public synchronized boolean remove(String id){return records.remove(id)!=null;}
 public synchronized Map<String,Map<String,String>> snapshot(){return Collections.unmodifiableMap(new LinkedHashMap<String,Map<String,String>>(records));}
}