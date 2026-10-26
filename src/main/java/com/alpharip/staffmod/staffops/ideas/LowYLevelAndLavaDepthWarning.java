package com.alpharip.staffmod.staffops.ideas;

/** Foundation for configurable low-Y-level and lava-depth warnings. */
public class LowYLevelAndLavaDepthWarning {
 private boolean enabled=true; private int lowYThreshold=12,lavaYThreshold=11;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public int getLowYThreshold(){return lowYThreshold;} public void setLowYThreshold(int v){lowYThreshold=v;}
 public int getLavaYThreshold(){return lavaYThreshold;} public void setLavaYThreshold(int v){lavaYThreshold=v;}
 public boolean shouldWarnLowY(int y){return enabled&&y<=lowYThreshold;}
 public boolean shouldWarnLavaDepth(int y){return enabled&&y<=lavaYThreshold;}
}
