package com.alpharip.staffmod.staffops.ideas;

/** Stores a dark palette and brightness preference; no framebuffer changes are performed. */
public final class NightVisionPalette {
 public static final String FEATURE_NAME="Night Vision Palette",CATEGORY="Donator Rogue",STATUS="NEW";
 private String palette="midnight"; private float brightness=1.0f;
 public String getPalette(){return palette;} public void setPalette(String p){if(p==null||p.trim().isEmpty())throw new IllegalArgumentException("palette required");palette=p.trim();}
 public float getBrightness(){return brightness;} public void setBrightness(float b){if(Float.isNaN(b)||b<0.5f||b>2.0f)throw new IllegalArgumentException("brightness must be 0.5-2.0");brightness=b;}
}