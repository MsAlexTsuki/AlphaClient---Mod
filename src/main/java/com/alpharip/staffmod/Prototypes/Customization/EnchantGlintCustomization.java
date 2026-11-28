package com.alpharip.staffmod.staffops.ideas;
/** Enchantment-glint preferences; rendering hooks are not connected. */
public class EnchantGlintCustomization {
 public enum Mode { DEFAULT, DISABLED, REDUCED, CUSTOM_COLOR }
 private boolean enabled=true;private Mode mode=Mode.DEFAULT;private int rgb=0xAA55FF;private float intensity=1F;
 public boolean isEnabled(){return enabled;}public void setEnabled(boolean v){enabled=v;}public Mode getMode(){return mode;}public void setMode(Mode m){if(m==null)throw new IllegalArgumentException("Mode required");mode=m;}
 public int getRgb(){return rgb;}public void setRgb(int c){rgb=c&0xFFFFFF;}public float getIntensity(){return intensity;}public void setIntensity(float v){if(Float.isNaN(v)||Float.isInfinite(v))throw new IllegalArgumentException("Invalid intensity");intensity=Math.max(0,Math.min(1,v));}
}