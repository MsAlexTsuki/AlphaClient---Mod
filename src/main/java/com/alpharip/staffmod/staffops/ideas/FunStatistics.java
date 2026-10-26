package com.alpharip.staffmod.staffops.ideas;

/** Foundation for tracking miscellaneous client statistics. */
public class FunStatistics {
 private final java.util.Map<String,Long> counters=new java.util.LinkedHashMap<String,Long>();
 public void increment(String name){add(name,1L);}
 public void add(String name,long amount){if(name!=null&&!name.trim().isEmpty())counters.put(name,(counters.containsKey(name)?counters.get(name):0L)+amount);}
 public long get(String name){Long value=counters.get(name);return value==null?0L:value;}
 public java.util.Map<String,Long> getAll(){return java.util.Collections.unmodifiableMap(counters);}
}
