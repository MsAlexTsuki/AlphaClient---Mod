package com.alpharip.staffmod.staffops.ideas;

/** Foundation for level-up confetti effect settings and triggering. */
public class LevelUpConfetti {
 private boolean enabled=true; private int particleCount=40; private long lastTriggeredMillis;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public int getParticleCount(){return particleCount;} public void setParticleCount(int v){particleCount=Math.max(0,Math.min(250,v));}
 public void trigger(long timestampMillis){if(enabled)lastTriggeredMillis=timestampMillis;}
 public long getLastTriggeredMillis(){return lastTriggeredMillis;}
}
