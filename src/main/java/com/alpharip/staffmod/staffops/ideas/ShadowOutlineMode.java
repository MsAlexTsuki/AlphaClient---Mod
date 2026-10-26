package com.alpharip.staffmod.staffops.ideas;

/** Stores outline-mode preferences; actual entity rendering is external. */
public final class ShadowOutlineMode {
 public static final String FEATURE_NAME="Shadow Outline Mode",CATEGORY="Donator Rogue",STATUS="NEW";
 private boolean enabled=true; private float width=1.0f;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public float getWidth(){return width;}
 public void setWidth(float value){if(Float.isNaN(value)||value<0.25f||value>4.0f)throw new IllegalArgumentException("width must be 0.25-4.0");width=value;}
}