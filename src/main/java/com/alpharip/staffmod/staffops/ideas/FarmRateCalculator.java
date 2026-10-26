package com.alpharip.staffmod.staffops.ideas;

/** Foundation for calculating farm output rates. */
public class FarmRateCalculator {
 private long startMillis; private int itemsCollected;
 public void start(long timestampMillis){startMillis=timestampMillis;itemsCollected=0;} public void recordItems(int count){if(count>0)itemsCollected+=count;} public int getItemsCollected(){return itemsCollected;} public double getItemsPerHour(long nowMillis){long elapsed=nowMillis-startMillis;return elapsed<=0?0.0:itemsCollected*3600000.0/elapsed;}
}
