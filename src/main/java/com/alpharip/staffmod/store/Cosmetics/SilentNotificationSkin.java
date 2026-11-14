package com.alpharip.staffmod.staffops.ideas;

/** Configures subtle notification presentation without sending notifications. */
public final class SilentNotificationSkin {
 public static final String FEATURE_NAME="Silent Notification Skin",CATEGORY="Donator Rogue",STATUS="NEW";
 private boolean soundEnabled=false; private long displayMillis=2500L; private float opacity=0.72f;
 public boolean isSoundEnabled(){return soundEnabled;} public void setSoundEnabled(boolean v){soundEnabled=v;}
 public long getDisplayMillis(){return displayMillis;} public void setDisplayMillis(long v){if(v<500||v>10000)throw new IllegalArgumentException("duration must be 500-10000 ms");displayMillis=v;}
 public float getOpacity(){return opacity;} public void setOpacity(float v){if(Float.isNaN(v)||v<0.1f||v>1f)throw new IllegalArgumentException("opacity must be 0.1-1.0");opacity=v;}
}