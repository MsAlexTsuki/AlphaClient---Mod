package com.alpharip.staffmod.staffops.ideas;

/** Player identity and display label supplied for an evidence annotation. */
public final class EvidencePlayerMarkerTool {
 private String playerId="",displayName="",note="";
 public void setMarker(String playerId,String displayName,String note){this.playerId=playerId==null?"":playerId;this.displayName=displayName==null?"":displayName;this.note=note==null?"":note;}
 public String getPlayerId(){return playerId;}public String getDisplayName(){return displayName;}public String getNote(){return note;}
}