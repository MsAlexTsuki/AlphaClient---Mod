package com.alpharip.staffmod.staffops.ideas;

/** Stores a soundscape preset and volume; audio playback is external. */
public final class CustomMenuSoundscape {
 public static final String FEATURE_NAME="Custom Menu Soundscape",CATEGORY="Donator Alpha+",STATUS="NEW";
 private String preset="default"; private float volume=0.5f;
 public String getPreset(){return preset;} public void setPreset(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("preset required");preset=s.trim();}
 public float getVolume(){return volume;} public void setVolume(float v){if(Float.isNaN(v)||v<0||v>1)throw new IllegalArgumentException("volume must be 0-1");volume=v;}
}