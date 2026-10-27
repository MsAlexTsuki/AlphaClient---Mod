package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/** Evaluates externally supplied resource samples against thresholds; live JVM/Forge sampling is not wired. */
public final class LiveResourceBudgetMonitor {
    public static final String NAME = "Live Resource Budget Monitor";
    public static final String CATEGORY = "Performance";
    public static final class Sample {
        public final long timestampMillis;
        public final double cpuPercent, memoryUsedMiB, frameTimeMillis;
        private Sample(long t,double c,double m,double f){timestampMillis=t;cpuPercent=c;memoryUsedMiB=m;frameTimeMillis=f;}
    }
    private final Deque<Sample> samples=new ArrayDeque<Sample>();
    private int capacity=300;
    private double cpuLimitPercent=90.0,memoryLimitMiB=2048.0,frameTimeLimitMillis=33.3;
    public synchronized void setLimits(double cpu,double memoryMiB,double frameTimeMs){
        if(!finite(cpu)||!finite(memoryMiB)||!finite(frameTimeMs)||cpu<0||cpu>100||memoryMiB<0||frameTimeMs<0)throw new IllegalArgumentException("Invalid resource limits");
        cpuLimitPercent=cpu;memoryLimitMiB=memoryMiB;frameTimeLimitMillis=frameTimeMs;
    }
    public synchronized void setCapacity(int n){if(n<1||n>10000)throw new IllegalArgumentException("Capacity must be 1-10000");capacity=n;trim();}
    public synchronized void record(long time,double cpu,double memoryMiB,double frameTimeMs){
        if(!finite(cpu)||!finite(memoryMiB)||!finite(frameTimeMs)||cpu<0||memoryMiB<0||frameTimeMs<0)throw new IllegalArgumentException("Invalid resource sample");
        samples.addLast(new Sample(Math.max(0,time),Math.min(100,cpu),memoryMiB,frameTimeMs));trim();
    }
    private void trim(){while(samples.size()>capacity)samples.removeFirst();}
    public synchronized List<String> getCurrentWarnings(){
        if(samples.isEmpty())return Collections.emptyList();Sample s=samples.peekLast();List<String>w=new ArrayList<String>();
        if(s.cpuPercent>cpuLimitPercent)w.add("CPU usage exceeded limit");
        if(s.memoryUsedMiB>memoryLimitMiB)w.add("Memory usage exceeded limit");
        if(s.frameTimeMillis>frameTimeLimitMillis)w.add("Frame time exceeded limit");
        return Collections.unmodifiableList(w);
    }
    public synchronized List<Sample> snapshot(){return Collections.unmodifiableList(new ArrayList<Sample>(samples));}
    public synchronized void clear(){samples.clear();}
    private static boolean finite(double n){return !Double.isNaN(n)&&!Double.isInfinite(n);}
}