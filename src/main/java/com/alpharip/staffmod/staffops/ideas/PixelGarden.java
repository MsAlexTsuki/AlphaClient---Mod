package com.alpharip.staffmod.staffops.ideas;

/** Models garden growth from playtime; rendering and persistence are external. */
public final class PixelGarden {
 public static final String FEATURE_NAME="Pixel Garden",CATEGORY="Founder Fun",STATUS="NEW";
 private long playtimeMinutes;
 public synchronized void addPlaytime(long minutes){if(minutes<0)throw new IllegalArgumentException("minutes must be non-negative");playtimeMinutes+=minutes;}
 public synchronized long getPlaytimeMinutes(){return playtimeMinutes;}
 public synchronized int getGrowthStage(){return (int)Math.min(10,playtimeMinutes/60);}
 public synchronized double getGrowthProgress(){return Math.min(1.0,(playtimeMinutes%60)/60.0);}
}