package com.alpharip.staffmod.AlphaCrashReporter;
import java.util.*;
public final class CrashPatternGrouper {
 public List<CrashPattern> group(List<CrashReport> reports){
  Map<String,List<CrashReport>> map=new LinkedHashMap<String,List<CrashReport>>();
  if(reports!=null)for(CrashReport r:reports){String k=r.getFingerprint().isEmpty()?r.getId():r.getFingerprint();if(!map.containsKey(k))map.put(k,new ArrayList<CrashReport>());map.get(k).add(r);}
  List<CrashPattern> out=new ArrayList<CrashPattern>();
  for(Map.Entry<String,List<CrashReport>> e:map.entrySet()){long first=Long.MAX_VALUE,last=0;CrashReport sample=e.getValue().get(0);
   for(CrashReport r:e.getValue()){first=Math.min(first,r.getTimestamp());last=Math.max(last,r.getTimestamp());}
   out.add(new CrashPattern(e.getKey(),e.getValue().size(),first,last,sample.getTitle()));}
  Collections.sort(out,(a,b)->Integer.compare(b.getOccurrences(),a.getOccurrences()));return out;
 }
}
