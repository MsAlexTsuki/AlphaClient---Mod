package com.alpharip.staffmod.RuleReferenceOverlay;
public final class RuleReferenceConfig {
 private boolean enabled=true;private int maxResults=8;
 public boolean isEnabled(){return enabled;}public void setEnabled(boolean v){enabled=v;}
 public int getMaxResults(){return maxResults;}public void setMaxResults(int v){if(v<1||v>50)throw new IllegalArgumentException("maxResults must be 1..50");maxResults=v;}
}