package com.alpharip.staffmod.staffops.ideas;

/** Metadata for a packet event supplied by a caller; no packet interception is performed. */
public final class EvidencePacketMarker {
 private String packetType="",direction="",summary=""; private long timestampMillis;
 public void setPacket(String packetType,String direction,String summary,long timestampMillis){this.packetType=packetType==null?"":packetType;this.direction=direction==null?"":direction;this.summary=summary==null?"":summary;this.timestampMillis=timestampMillis;}
 public String getPacketType(){return packetType;} public String getDirection(){return direction;} public String getSummary(){return summary;} public long getTimestampMillis(){return timestampMillis;}
}