package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Heuristic response-interval TPS estimate, not authoritative server TPS. */
public class TPSEstimator {
 private final Deque<Long> intervals=new ArrayDeque<Long>();private long last=-1;private int capacity=20;
 public void recordResponse(long millis){if(millis<0)throw new IllegalArgumentException();if(last>=0&&millis>=last){intervals.addLast(Math.max(1,millis-last));while(intervals.size()>capacity)intervals.removeFirst();}last=millis;}
 public double estimateTps(){if(intervals.isEmpty())return 20.0;long sum=0;for(Long x:intervals)sum+=x;return Math.min(20.0,1000.0/((double)sum/intervals.size()));}
 public List<Long> getIntervals(){return Collections.unmodifiableList(new ArrayList<Long>(intervals));}public void reset(){intervals.clear();last=-1;}
}