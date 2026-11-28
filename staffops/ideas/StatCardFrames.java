package com.alpharip.staffmod.staffops.ideas;

/** Stores a selected cosmetic statistics-card frame. */
public final class StatCardFrames {
 public static final String FEATURE_NAME="Stat Card Frames",CATEGORY="Donator Elite",STATUS="NEW";
 private String frame="classic";
 public String getFrame(){return frame;} public void setFrame(String value){if(value==null||value.trim().isEmpty())throw new IllegalArgumentException("frame required");frame=value.trim();}
}