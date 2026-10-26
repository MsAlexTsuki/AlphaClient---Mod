package com.alpharip.staffmod.staffops.ideas;

/** Foundation for recording the latest source of player damage. */
public class LastDamageSource {
 private String source=""; private long timestampMillis;
 public void record(String source,long timestampMillis){this.source=source==null?"Unknown":source;this.timestampMillis=timestampMillis;}
 public String getSource(){return source;} public long getTimestampMillis(){return timestampMillis;}
 public void clear(){source="";timestampMillis=0L;}
}
