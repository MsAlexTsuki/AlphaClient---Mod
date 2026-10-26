package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Tracks strafe-efficiency samples; overlay/event wiring remains to be added. */
public class StrafeTrainerOverlay {
 private boolean enabled=true;private final Deque<Double> samples=new ArrayDeque<Double>();private int capacity=120;
 public boolean isEnabled(){return enabled;}public void setEnabled(boolean v){enabled=v;}public void setCapacity(int n){if(n<1||n>2000)throw new IllegalArgumentException("Capacity must be 1-2000");capacity=n;trim();}
 public void recordStrafeEfficiency(double p){if(Double.isNaN(p)||Double.isInfinite(p))return;samples.addLast(Math.max(0,Math.min(100,p)));trim();}private void trim(){while(samples.size()>capacity)samples.removeFirst();}
 public double getAverageEfficiency(){if(samples.isEmpty())return 0;double s=0;for(double d:samples)s+=d;return s/samples.size();}public List<Double> getSamples(){return Collections.unmodifiableList(new ArrayList<Double>(samples));}public void reset(){samples.clear();}
}