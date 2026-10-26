package com.alpharip.staffmod.staffops.ideas;

/** Foundation for tracking elapsed time between hits. */
public class HitDelayIndicator {
 private long lastHitMillis=-1L; private long lastIntervalMillis; private int hitsRecorded;
 public void recordHit(long timestampMillis){if(lastHitMillis>=0)lastIntervalMillis=Math.max(0L,timestampMillis-lastHitMillis);lastHitMillis=timestampMillis;hitsRecorded++;} public long getLastIntervalMillis(){return lastIntervalMillis;} public int getHitsRecorded(){return hitsRecorded;}
}
