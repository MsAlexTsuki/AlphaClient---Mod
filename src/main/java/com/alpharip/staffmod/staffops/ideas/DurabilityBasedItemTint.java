package com.alpharip.staffmod.staffops.ideas;

/** Foundation for tinting item indicators based on remaining durability. */
public class DurabilityBasedItemTint {
 private boolean enabled=true; private float warningThreshold=0.25f;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public float getWarningThreshold(){return warningThreshold;} public void setWarningThreshold(float v){warningThreshold=Math.max(0f,Math.min(1f,v));}
 public float getRemainingRatio(int remaining,int maximum){if(maximum<=0)return 1f;return Math.max(0f,Math.min(1f,(float)remaining/maximum));}
 public boolean isLowDurability(int remaining,int maximum){return enabled&&maximum>0&&getRemainingRatio(remaining,maximum)<=warningThreshold;}
}
