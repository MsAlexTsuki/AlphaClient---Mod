package com.alpharip.staffmod.staffops.ideas;

/** Foundation for a custom experience-bar color. */
public class CustomXpBarColor {
 private boolean enabled; private int color=0xFF55FF55;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public int getColor(){return color;} public void setColor(int argb){color=argb;}
 public int getEffectiveColor(int vanillaColor){return enabled?color:vanillaColor;}
}
