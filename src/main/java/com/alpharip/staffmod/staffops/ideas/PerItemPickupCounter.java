package com.alpharip.staffmod.staffops.ideas;

/** Foundation for per-item pickup counts within a session. */
public class PerItemPickupCounter {
 private final java.util.Map<String,Long> counts=new java.util.LinkedHashMap<String,Long>();
 public void record(String itemId,int amount){if(itemId!=null&&!itemId.trim().isEmpty()&&amount>0)counts.put(itemId,(counts.containsKey(itemId)?counts.get(itemId):0L)+amount);}
 public long getCount(String itemId){Long v=counts.get(itemId);return v==null?0L:v;}
 public void reset(){counts.clear();}
 public java.util.Map<String,Long> getCounts(){return java.util.Collections.unmodifiableMap(counts);}
}
