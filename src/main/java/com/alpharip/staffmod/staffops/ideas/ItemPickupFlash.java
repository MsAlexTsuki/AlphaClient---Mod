package com.alpharip.staffmod.staffops.ideas;

/** Foundation for a brief visual flash when an item is picked up. */
public class ItemPickupFlash {
 private boolean enabled=true; private long lastPickupMillis; private int durationMillis=250;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public int getDurationMillis(){return durationMillis;} public void setDurationMillis(int v){durationMillis=Math.max(0,Math.min(3000,v));}
 public void trigger(long timestampMillis){lastPickupMillis=timestampMillis;}
 public boolean isActive(long nowMillis){return enabled&&nowMillis>=lastPickupMillis&&nowMillis-lastPickupMillis<durationMillis;}
 public float getProgress(long nowMillis){if(!isActive(nowMillis)||durationMillis==0)return 0f;return 1f-(float)(nowMillis-lastPickupMillis)/durationMillis;}
}
