package com.alpharip.staffmod.staffops.ideas;

/** Stores group banner text and a theme identifier for a future banner renderer. */
public final class GroupBannerMaker {
 public static final String FEATURE_NAME="Group Banner Maker",CATEGORY="Donator Alpha",STATUS="NEW";
 private String title="",subtitle="",theme="default";
 public void setTitle(String s){title=clean(s);} public void setSubtitle(String s){subtitle=clean(s);}
 public void setTheme(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("theme required");theme=s.trim();}
 public String getTitle(){return title;} public String getSubtitle(){return subtitle;} public String getTheme(){return theme;}
 private String clean(String s){return s==null?"":s.trim();}
}