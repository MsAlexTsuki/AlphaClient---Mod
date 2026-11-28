package com.alpharip.staffmod.staffops.ideas;

/** Holds a chat excerpt and timestamp for evidence annotation. */
public final class EvidenceChatMarker {
 private String speaker="",message="";private long timestampMillis;
 public void setMessage(String speaker,String message,long timestampMillis){this.speaker=speaker==null?"":speaker;this.message=message==null?"":message;this.timestampMillis=timestampMillis;}
 public String getSpeaker(){return speaker;}public String getMessage(){return message;}public long getTimestampMillis(){return timestampMillis;}
}