package com.alpharip.staffmod.staffops.ideas;

/** Builds a concise recap from supplied raid details; does not collect server events. */
public final class FactionRaidRecapCard {
 public static final String FEATURE_NAME="Faction Raid Recap Card",CATEGORY="Cross-Category",STATUS="NEW";
 public String create(String faction,String outcome,int participants,String notes){
  if(faction==null||faction.trim().isEmpty())throw new IllegalArgumentException("faction required");
  if(participants<0)throw new IllegalArgumentException("participants cannot be negative");
  return faction.trim()+" raid recap: "+clean(outcome,"Outcome not recorded")+". Participants: "+participants+". "+clean(notes,"");
 }
 private String clean(String s,String fallback){return s==null||s.trim().isEmpty()?fallback:s.trim();}
}