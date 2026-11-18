package com.alpharip.staffmod.BuildTimelapse.effects;
public final class FadeEffect {
 private final long start,duration;private final boolean in;
 public FadeEffect(long s,long d,boolean i){if(d<=0)throw new IllegalArgumentException();start=s;duration=d;in=i;}
 public float alpha(long time){double p=Math.max(0,Math.min(1,((double)time-(double)start)/(double)duration));return(float)(in?1-p:p);}
}