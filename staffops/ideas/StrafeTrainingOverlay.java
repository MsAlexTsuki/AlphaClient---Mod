package com.alpharip.staffmod.staffops.ideas;

/** Foundation for strafe training metrics. */
public class StrafeTrainingOverlay {
 private boolean enabled; private int directionChanges; private long sessionMillis;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public void recordDirectionChange(){directionChanges++;} public int getDirectionChanges(){return directionChanges;}
 public long getSessionMillis(){return sessionMillis;} public void setSessionMillis(long v){sessionMillis=Math.max(0L,v);}
}
