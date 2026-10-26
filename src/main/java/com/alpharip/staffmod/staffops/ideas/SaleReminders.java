package com.alpharip.staffmod.staffops.ideas;

/** Foundation for local reminders about scheduled sales or events. */
public class SaleReminders {
 private final java.util.Map<String,Long> reminders=new java.util.LinkedHashMap<String,Long>();
 public void add(String label,long timestampMillis){if(label!=null&&!label.trim().isEmpty())reminders.put(label,timestampMillis);}
 public Long getTimestamp(String label){return reminders.get(label);}
 public boolean remove(String label){return reminders.remove(label)!=null;}
 public java.util.Map<String,Long> getReminders(){return java.util.Collections.unmodifiableMap(reminders);}
}
