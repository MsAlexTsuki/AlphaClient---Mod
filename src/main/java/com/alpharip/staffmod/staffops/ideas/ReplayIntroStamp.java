package com.alpharip.staffmod.staffops.ideas;

/** Formats a replay title card label from caller-supplied details. */
public final class ReplayIntroStamp {
 public static final String FEATURE_NAME="Replay Intro Stamp",CATEGORY="Donator Elite",STATUS="NEW";
 public String format(String player,String replayTitle){return clean(player,"Player")+" — "+clean(replayTitle,"Replay");}
 private String clean(String s,String d){return s==null||s.trim().isEmpty()?d:s.trim();}
}