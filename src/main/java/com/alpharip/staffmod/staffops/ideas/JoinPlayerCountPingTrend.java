package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Bounded trend history for player count and ping samples. */
public class JoinPlayerCountPingTrend {public static final class Sample {public final long time;public final int players,ping;public Sample(long t,int p,int g){if(t<0||p<0||g<0)throw new IllegalArgumentException();time=t;players=p;ping=g;}}
 private final Deque<Sample> samples=new ArrayDeque<Sample>();private int capacity=60;
 public void setCapacity(int n){if(n<1)throw new IllegalArgumentException();capacity=n;trim();}public void add(long t,int players,int ping){samples.addLast(new Sample(t,players,ping));trim();}
 public List<Sample> getSamples(){return Collections.unmodifiableList(new ArrayList<Sample>(samples));}
 public double averagePing(){if(samples.isEmpty())return 0;long sum=0;for(Sample s:samples)sum+=s.ping;return (double)sum/samples.size();}
 public int playerCountChange(){return samples.size()<2?0:samples.getLast().players-samples.getFirst().players;}private void trim(){while(samples.size()>capacity)samples.removeFirst();}
}