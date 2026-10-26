package com.alpharip.staffmod.staffops.ideas;
/** Badge display state; actual staff mode must come from the existing staff permission system. */
public class StaffModeBadge {private boolean active;private String label="STAFF MODE";private int color=0xFF5555;
 public boolean isActive(){return active;}public void setActive(boolean v){active=v;}public String getLabel(){return label;}public void setLabel(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException();label=s.trim();}
 public int getColor(){return color;}public void setColor(int rgb){if(rgb<0||rgb>0xFFFFFF)throw new IllegalArgumentException();color=rgb;}
}