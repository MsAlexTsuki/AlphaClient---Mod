package com.alpharip.staffmod.staffops.ideas;

/** Foundation for reducing or disabling camera shake effects. */
public class ReduceScreenShake {
 private boolean enabled=true; private float intensity=0.35f;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public float getIntensity(){return intensity;} public void setIntensity(float v){intensity=Math.max(0f,Math.min(1f,v));}
 public float apply(float vanillaIntensity){return enabled?vanillaIntensity*intensity:vanillaIntensity;}
}
