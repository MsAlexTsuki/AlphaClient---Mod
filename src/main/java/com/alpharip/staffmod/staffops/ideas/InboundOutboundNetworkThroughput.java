package com.alpharip.staffmod.staffops.ideas;

/** Foundation for recording inbound and outbound network throughput samples. */
public class InboundOutboundNetworkThroughput {
 private long inboundBytesPerSecond,outboundBytesPerSecond;
 public void update(long inbound,long outbound){inboundBytesPerSecond=Math.max(0L,inbound);outboundBytesPerSecond=Math.max(0L,outbound);}
 public long getInboundBytesPerSecond(){return inboundBytesPerSecond;} public long getOutboundBytesPerSecond(){return outboundBytesPerSecond;}
 public double getInboundKilobytesPerSecond(){return inboundBytesPerSecond/1024.0;}
 public double getOutboundKilobytesPerSecond(){return outboundBytesPerSecond/1024.0;}
}
