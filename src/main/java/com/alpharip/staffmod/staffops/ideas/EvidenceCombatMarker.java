package com.alpharip.staffmod.staffops.ideas;

/** Describes a combat-related event from explicitly supplied client-side data. */
public final class EvidenceCombatMarker {
 private String eventType="",actor="",target="";private long timestampMillis;
 public void setEvent(String eventType,String actor,String target,long timestampMillis){this.eventType=eventType==null?"":eventType;this.actor=actor==null?"":actor;this.target=target==null?"":target;this.timestampMillis=timestampMillis;}
 public String getEventType(){return eventType;}public String getActor(){return actor;}public String getTarget(){return target;}public long getTimestampMillis(){return timestampMillis;}
}