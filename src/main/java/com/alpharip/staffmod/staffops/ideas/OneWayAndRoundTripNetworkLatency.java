package com.alpharip.staffmod.staffops.ideas;

/** Foundation for network latency measurements. One-way latency requires synchronized clocks or server-side measurement. */
public class OneWayAndRoundTripNetworkLatency {
 private long oneWayMillis=-1L, roundTripMillis=-1L;
 public void update(long oneWay,long roundTrip){oneWayMillis=oneWay<0?-1L:oneWay;roundTripMillis=roundTrip<0?-1L:roundTrip;}
 public long getOneWayMillis(){return oneWayMillis;} public long getRoundTripMillis(){return roundTripMillis;}
}
