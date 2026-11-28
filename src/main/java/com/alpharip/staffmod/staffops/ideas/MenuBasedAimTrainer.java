package com.alpharip.staffmod.staffops.ideas;

/** Foundation for menu-based aim-training sessions; no gameplay hooks are wired yet. */
public class MenuBasedAimTrainer {
 private int targetsHit, targetsMissed; private long elapsedMillis;
 public void recordHit(){targetsHit++;} public void recordMiss(){targetsMissed++;}
 public int getTargetsHit(){return targetsHit;} public int getTargetsMissed(){return targetsMissed;} public long getElapsedMillis(){return elapsedMillis;}
 public void setElapsedMillis(long v){elapsedMillis=Math.max(0L,v);} public double getAccuracy(){int n=targetsHit+targetsMissed;return n==0?0.0:(double)targetsHit*100.0/n;}
}
