package com.alpharip.staffmod.staffops.ideas;

/** Foundation for recording lag-spike samples and candidate causes. */
public class LagSpikeDetectorWithLikelyCause {
 public enum Cause { UNKNOWN, CPU, GPU, MEMORY, NETWORK, CHUNK_LOADING }
 private long lastSpikeAt; private long frameTimeMillis; private Cause likelyCause=Cause.UNKNOWN;
 public void recordSpike(long at,long frame,Cause cause){lastSpikeAt=Math.max(0L,at);frameTimeMillis=Math.max(0L,frame);likelyCause=cause==null?Cause.UNKNOWN:cause;}
 public long getLastSpikeAt(){return lastSpikeAt;} public long getFrameTimeMillis(){return frameTimeMillis;} public Cause getLikelyCause(){return likelyCause;}
}
