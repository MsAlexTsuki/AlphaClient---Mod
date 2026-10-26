package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Totals stack counts by item identifier from an inventory snapshot. */
public class ChestItemTotal {public Map<String,Integer> totals(Map<String,Integer> slots){Map<String,Integer> out=new LinkedHashMap<String,Integer>();if(slots!=null)for(Map.Entry<String,Integer> e:slots.entrySet()){if(e.getKey()==null||e.getValue()==null||e.getValue()<=0)continue;Integer old=out.get(e.getKey());out.put(e.getKey(),(old==null?0:old)+e.getValue());}return Collections.unmodifiableMap(out);}
 public int totalItems(Map<String,Integer> slots){int n=0;if(slots!=null)for(Integer v:slots.values())if(v!=null&&v>0)n+=v;return n;}
}