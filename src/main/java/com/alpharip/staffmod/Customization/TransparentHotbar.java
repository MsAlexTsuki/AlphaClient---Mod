package com.alpharip.staffmod.staffops.ideas;

/** Foundation for configuring hotbar background transparency. */
public class TransparentHotbar {
 private boolean enabled; private float opacity=1.0f;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public float getOpacity(){return opacity;} public void setOpacity(float v){opacity=Math.max(0f,Math.min(1f,v));}
 public float getEffectiveOpacity(){return enabled?opacity:1.0f;}
}
