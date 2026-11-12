package com.alpharip.staffmod.staffops.ideas;

/** Foundation for controlling enchantment-glint visibility and intensity. */
public class EnchantmentGlintSettings {
 private boolean disabled; private float intensity=1.0f;
 public boolean isDisabled(){return disabled;} public void setDisabled(boolean v){disabled=v;}
 public float getIntensity(){return intensity;} public void setIntensity(float v){intensity=Math.max(0f,Math.min(1f,v));}
 public boolean shouldRender(){return !disabled&&intensity>0f;}
}
