package com.alpharip.staffmod.staffops.ideas;

/** Formats a simple welcome banner from supplied player and rank names. */
public final class WelcomeBanner {
 public static final String FEATURE_NAME="Welcome Banner",CATEGORY="Donator Scout",STATUS="NEW";
 public String format(String player,String rank){String p=clean(player,"Player");String r=clean(rank,"Member");return "Welcome, "+p+"! Rank: "+r;}
 private String clean(String v,String fallback){return v==null||v.trim().isEmpty()?fallback:v.trim();}
}