package com.alpharip.staffmod.staffops.ideas;

/** Records a supplied interaction description for an evidence timeline. */
public final class EvidenceInteractionMarker {
 private String interactionType="",target="";private long timestampMillis;
 public void setInteraction(String interactionType,String target,long timestampMillis){this.interactionType=interactionType==null?"":interactionType;this.target=target==null?"":target;this.timestampMillis=timestampMillis;}
 public String getInteractionType(){return interactionType;}public String getTarget(){return target;}public long getTimestampMillis(){return timestampMillis;}
}