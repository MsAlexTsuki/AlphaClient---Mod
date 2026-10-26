package com.alpharip.staffmod.staffops.ideas;

/** Foundation for displaying estimated progress toward breaking the targeted block. */
public class TargetBlockBreakSpeedIndicator {
 private boolean enabled=true; private float progress; private float estimatedSecondsRemaining;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public void update(float progress,float secondsRemaining){this.progress=Math.max(0f,Math.min(1f,progress));estimatedSecondsRemaining=Math.max(0f,secondsRemaining);}
 public float getProgress(){return progress;} public float getEstimatedSecondsRemaining(){return estimatedSecondsRemaining;}
}
