package com.alpharip.staffmod.staffops.ideas;

/** Marker position and label for a future replay timeline. */
public final class EvidenceReplayMarker {
 private long replayTimeMillis; private String label="",note="";
 public void setMarker(long replayTimeMillis,String label,String note){this.replayTimeMillis=Math.max(0L,replayTimeMillis);this.label=label==null?"":label;this.note=note==null?"":note;}
 public long getReplayTimeMillis(){return replayTimeMillis;} public String getLabel(){return label;} public String getNote(){return note;}
}