package com.alpharip.staffmod.staffops.ideas;

/** Foundation for tracking potion timing practice. */
public class PotionTimingCoach {
 private long lastPotionUseMillis; private int usesRecorded;
 public void recordPotionUse(long timestampMillis){lastPotionUseMillis=timestampMillis;usesRecorded++;} public long getLastPotionUseMillis(){return lastPotionUseMillis;} public int getUsesRecorded(){return usesRecorded;}
}
