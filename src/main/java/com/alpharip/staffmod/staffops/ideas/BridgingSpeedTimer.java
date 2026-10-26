package com.alpharip.staffmod.staffops.ideas;

/** Foundation for tracking bridging speed during a session. */
public class BridgingSpeedTimer {
 private long startedAtMillis; private int blocksPlaced; private double blocksPerSecond;
 public void start(long nowMillis){startedAtMillis=nowMillis;blocksPlaced=0;blocksPerSecond=0.0;} public void recordBlock(long nowMillis){blocksPlaced++;long elapsed=nowMillis-startedAtMillis;if(elapsed>0)blocksPerSecond=blocksPlaced*1000.0/elapsed;} public int getBlocksPlaced(){return blocksPlaced;} public double getBlocksPerSecond(){return blocksPerSecond;}
}
