package com.alpharip.staffmod.staffops.ideas;

/** Foundation for timers created from detected chat events. */
public class ChatDetectedEventTimers {
 private final java.util.Map<String,Long> endTimes=new java.util.LinkedHashMap<String,Long>();
 public void start(String event,long nowMillis,long durationMillis){if(event!=null&&!event.trim().isEmpty())endTimes.put(event,nowMillis+Math.max(0L,durationMillis));}
 public long getRemainingMillis(String event,long nowMillis){Long end=endTimes.get(event);return end==null?0L:Math.max(0L,end-nowMillis);}
 public boolean remove(String event){return endTimes.remove(event)!=null;}
 public java.util.Map<String,Long> getEndTimes(){return java.util.Collections.unmodifiableMap(endTimes);}
}
