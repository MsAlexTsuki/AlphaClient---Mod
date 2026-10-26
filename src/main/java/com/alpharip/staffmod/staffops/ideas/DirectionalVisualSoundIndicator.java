package com.alpharip.staffmod.staffops.ideas;

/** Foundation for visualizing sound direction cues. */
public class DirectionalVisualSoundIndicator {
 private boolean enabled; private float directionDegrees; private float intensity;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public void update(float degrees,float strength){directionDegrees=((degrees%360.0f)+360.0f)%360.0f;intensity=Math.max(0.0f,Math.min(1.0f,strength));}
 public float getDirectionDegrees(){return directionDegrees;} public float getIntensity(){return intensity;}
}
