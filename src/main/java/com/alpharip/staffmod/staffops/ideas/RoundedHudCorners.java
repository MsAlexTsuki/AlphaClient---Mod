package com.alpharip.staffmod.staffops.ideas;

/** Foundation for rounded HUD corner styling. */
public class RoundedHudCorners {
 private boolean enabled=true; private int radius=5;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public int getRadius(){return radius;} public void setRadius(int v){radius=Math.max(0,Math.min(24,v));}
}
