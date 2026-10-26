package com.alpharip.staffmod.staffops.ideas;

/** Stores an animated profile frame selection and playback speed. */
public final class AnimatedProfileFrame {
 public static final String FEATURE_NAME="Animated Profile Frame",CATEGORY="Donator Alpha+",STATUS="NEW";
 private String frame="none"; private float speed=1.0f;
 public String getFrame(){return frame;} public void setFrame(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("frame required");frame=s.trim();}
 public float getSpeed(){return speed;} public void setSpeed(float v){if(Float.isNaN(v)||v<0.25f||v>3f)throw new IllegalArgumentException("speed must be 0.25-3");speed=v;}
}