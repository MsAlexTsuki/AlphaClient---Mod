package com.alpharip.staffmod.BuildTimelapse.effects;
public final class SpeedRamp {private final long start,end;private final double from,to;public SpeedRamp(long s,long e,double f,double t){if(e<=s||f<=0||t<=0)throw new IllegalArgumentException();start=s;end=e;from=f;to=t;}public double at(long time){double p=Math.max(0,Math.min(1,(double)(time-start)/(end-start)));return from+(to-from)*p;}}
