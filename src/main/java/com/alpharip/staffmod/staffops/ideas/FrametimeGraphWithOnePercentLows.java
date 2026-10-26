package com.alpharip.staffmod.staffops.ideas;

/** Foundation for frame-time samples and 1% low calculations. */
public class FrametimeGraphWithOnePercentLows {
 private final java.util.List<Double> samples=new java.util.ArrayList<Double>();
 public void addFrameTime(double ms){if(ms>0&&!Double.isInfinite(ms)&&!Double.isNaN(ms)){samples.add(ms);if(samples.size()>2000)samples.remove(0);}}
 public java.util.List<Double> getSamples(){return java.util.Collections.unmodifiableList(new java.util.ArrayList<Double>(samples));}
 public double getOnePercentLowFps(){if(samples.isEmpty())return 0;java.util.List<Double> sorted=new java.util.ArrayList<Double>(samples);java.util.Collections.sort(sorted);int i=Math.min(sorted.size()-1,(int)Math.floor((sorted.size()-1)*0.99));return sorted.get(i)<=0?0:1000.0/sorted.get(i);}
}
