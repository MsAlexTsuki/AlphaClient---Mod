package com.alpharip.staffmod.staffops.ideas;

/** Foundation for counting ores collected in the current session. */
public class SessionOreCounter {
 private final java.util.Map<String,Integer> oreCounts=new java.util.LinkedHashMap<String,Integer>();
 public void recordOre(String ore,int amount){if(ore==null||ore.trim().isEmpty()||amount<=0)return;oreCounts.put(ore,oreCounts.containsKey(ore)?oreCounts.get(ore)+amount:amount);} public int getCount(String ore){Integer count=oreCounts.get(ore);return count==null?0:count;} public java.util.Map<String,Integer> getCounts(){return java.util.Collections.unmodifiableMap(oreCounts);}
}
