package com.alpharip.staffmod.staffops.ideas;

/** Foundation for crosshair placement practice sessions. */
public class CrosshairPlacementPractice {
 private boolean enabled; private int targetsTracked; private int successfulPlacements;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;} public void recordTarget(){targetsTracked++;} public void recordSuccessfulPlacement(){successfulPlacements++;} public int getTargetsTracked(){return targetsTracked;} public int getSuccessfulPlacements(){return successfulPlacements;}
}
