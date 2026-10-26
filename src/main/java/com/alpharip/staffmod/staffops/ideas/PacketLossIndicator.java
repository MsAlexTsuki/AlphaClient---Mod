package com.alpharip.staffmod.staffops.ideas;

/** Foundation for a packet-loss estimate based on sent/received sequence counts. */
public class PacketLossIndicator {
 private long sent,received;
 public void update(long sentCount,long receivedCount){sent=Math.max(0L,sentCount);received=Math.max(0L,Math.min(receivedCount,sent));}
 public long getSent(){return sent;} public long getReceived(){return received;}
 public double getLossPercent(){return sent==0?0.0:((double)(sent-received)*100.0)/(double)sent;}
}
