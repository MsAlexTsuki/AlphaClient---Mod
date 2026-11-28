package com.alpharip.staffmod.staffops.ideas;
/** Tracks peak CPS and longest completed play session. */
public class FunStats {private int peakCps;private long sessionStart=-1,longest;
 public void recordCps(int cps){if(cps<0)throw new IllegalArgumentException();peakCps=Math.max(peakCps,cps);}
 public void startSession(long now){if(now<0)throw new IllegalArgumentException();if(sessionStart<0)sessionStart=now;}
 public long endSession(long now){if(sessionStart<0)return 0;if(now<sessionStart)throw new IllegalArgumentException();long d=now-sessionStart;longest=Math.max(longest,d);sessionStart=-1;return d;}
 public int getPeakCps(){return peakCps;}public long getLongestSessionMillis(){return longest;}
}