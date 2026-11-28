package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** AppealReviewWorkspace. Organises appeal review materials and reviewer notes. Local foundation only; Forge integration is pending. */
public final class AppealReviewWorkspace {
 public static final String NAME="Appeal Review Workspace", CATEGORY="Senior Staff", STATUS="NEW";
 public static final String PURPOSE="Organises appeal review materials and reviewer notes."; public static final boolean REQUIRES_SUPABASE=true, REQUIRES_SERVER_SUPPORT=true;
 TODO(Supabase): connect authorised appeal records later.
 // TODO(Server): access to appeals must be enforced by the server/plugin.
 private final Map<String,Map<String,String>> records=new LinkedHashMap<String,Map<String,String>>();
 public synchronized void put(String id,Map<String,String> fields){if(id==null||id.trim().isEmpty()||fields==null)throw new IllegalArgumentException("id and fields required");records.put(id.trim(),Collections.unmodifiableMap(new LinkedHashMap<String,String>(fields)));}
 public synchronized Map<String,String> get(String id){return records.get(id);}
 public synchronized boolean remove(String id){return records.remove(id)!=null;}
 public synchronized Map<String,Map<String,String>> snapshot(){return Collections.unmodifiableMap(new LinkedHashMap<String,Map<String,String>>(records));}
}