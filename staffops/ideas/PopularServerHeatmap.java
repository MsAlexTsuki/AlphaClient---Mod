package com.alpharip.staffmod.staffops.ideas;

/** Foundation for aggregated server popularity data. TODO(Supabase): store privacy-safe server visit aggregates. */
public class PopularServerHeatmap {
 private final java.util.Map<String,Long> visits=new java.util.LinkedHashMap<String,Long>();
 public void recordVisit(String server,long count){if(server!=null&&!server.trim().isEmpty()&&count>0)visits.put(server,(visits.containsKey(server)?visits.get(server):0L)+count);}
 public java.util.Map<String,Long> getVisits(){return java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<String,Long>(visits));}
}
