package com.alpharip.staffmod.staffops.ideas;

/** Foundation for inventory background appearance settings. */
public class CustomInventoryBackground {
 private boolean enabled; private int color=0xCC101010; private float opacity=0.8f;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public int getColor(){return color;} public void setColor(int v){color=v;}
 public float getOpacity(){return opacity;} public void setOpacity(float v){opacity=Math.max(0f,Math.min(1f,v));}
}
