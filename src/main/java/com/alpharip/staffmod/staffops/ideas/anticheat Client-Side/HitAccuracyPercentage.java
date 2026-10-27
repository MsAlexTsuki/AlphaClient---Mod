package com.alpharip.staffmod.staffops.ideas;

/** Foundation for tracking attempted hits and successful hits. */
public class HitAccuracyPercentage {
 private long attempts,hits;
 public void recordAttempt(boolean successful){attempts++;if(successful)hits++;}
 public long getAttempts(){return attempts;} public long getHits(){return hits;} public double getAccuracyPercent(){return attempts==0?0.0:(double)hits*100.0/attempts;}
}
