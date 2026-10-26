package com.alpharip.staffmod.staffops.ideas;

/** Foundation for the visual state of a block-break progress ring. */
public class BlockBreakProgressRing {
 private boolean enabled=true; private float progress; private int lineWidth=3;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public float getProgress(){return progress;} public void setProgress(float v){progress=Math.max(0f,Math.min(1f,v));}
 public int getLineWidth(){return lineWidth;} public void setLineWidth(int v){lineWidth=Math.max(1,Math.min(12,v));}
}
