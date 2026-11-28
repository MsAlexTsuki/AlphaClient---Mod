package com.alpharip.staffmod.IncidentPostMortemBoard; import java.util.*;
public final class IncidentPostMortemBoard {
 private boolean enabled=true;private final Map<String,IncidentRecord> incidents=new LinkedHashMap<String,IncidentRecord>();private final List<String> legacyRecords=new ArrayList<String>();
 public synchronized boolean isEnabled(){return enabled;} public synchronized void setEnabled(boolean v){enabled=v;}
 /** Backward-compatible API retained from the original StaffOps idea. */
 public synchronized void addRecord(String s){if(s!=null&&!s.trim().isEmpty())legacyRecords.add(s);} public synchronized List<String> getRecords(){return Collections.unmodifiableList(new ArrayList<String>(legacyRecords));} public synchronized void clearRecords(){legacyRecords.clear();}
 public synchronized void add(IncidentRecord r){if(r==null)throw new IllegalArgumentException("Incident required");if(incidents.containsKey(r.getId()))throw new IllegalArgumentException("Incident ID already exists");incidents.put(r.getId(),r);} public synchronized IncidentRecord get(String id){return incidents.get(id);} public synchronized boolean remove(String id){return incidents.remove(id)!=null;} public synchronized List<IncidentRecord> getAll(){return Collections.unmodifiableList(new ArrayList<IncidentRecord>(incidents.values()));} public synchronized int size(){return incidents.size();}
 public synchronized void replaceAll(List<IncidentRecord> list){incidents.clear();if(list!=null)for(IncidentRecord r:list)if(r!=null&&!incidents.containsKey(r.getId()))incidents.put(r.getId(),r);}
}