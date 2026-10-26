package com.alpharip.staffmod.staffops.ideas;

/** Foundation for estimating mining throughput per hour. */
public class MiningStatisticsPerHour {
 private long sessionStartMillis; private int blocksMined;
 public void start(long timestampMillis){sessionStartMillis=timestampMillis;blocksMined=0;} public void recordBlocks(int count){if(count>0)blocksMined+=count;} public int getBlocksMined(){return blocksMined;} public double getBlocksPerHour(long nowMillis){long elapsed=nowMillis-sessionStartMillis;return elapsed<=0?0.0:blocksMined*3600000.0/elapsed;}
}
