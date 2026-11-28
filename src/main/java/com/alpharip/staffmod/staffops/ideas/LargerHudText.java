package com.alpharip.staffmod.staffops.ideas;

/** Foundation for configurable HUD text scaling. */
public class LargerHudText {
 private float scale=1.0f;
 public float getScale(){return scale;}
 public void setScale(float value){scale=Math.max(0.5f,Math.min(3.0f,value));}
}
