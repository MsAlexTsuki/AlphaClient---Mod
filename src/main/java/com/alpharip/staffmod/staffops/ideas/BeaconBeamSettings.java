package com.alpharip.staffmod.staffops.ideas;

/** Foundation for beacon beam visibility and thickness preferences. */
public class BeaconBeamSettings {
 private boolean hidden; private float thickness=1.0f;
 public boolean isHidden(){return hidden;} public void setHidden(boolean v){hidden=v;}
 public float getThickness(){return thickness;} public void setThickness(float v){thickness=Math.max(0.05f,Math.min(1f,v));}
 public boolean shouldRender(){return !hidden;}
}
