package com.alpharip.staffmod.BuildTimelapse.analytics;
import java.util.*;
public final class MilestoneDetector {
 private final int[] thresholds;private final Set<Integer> emitted=new HashSet<Integer>();
 public MilestoneDetector(int... values){TreeSet<Integer> sorted=new TreeSet<Integer>();if(values!=null)for(int n:values){if(n<=0)throw new IllegalArgumentException("Milestone thresholds must be positive");sorted.add(n);}thresholds=new int[sorted.size()];int i=0;for(Integer n:sorted)thresholds[i++]=n;}
 public synchronized List<String> update(long placements){List<String> out=new ArrayList<String>();for(int n:thresholds)if(placements>=n&&emitted.add(n))out.add(n+" blocks placed");return Collections.unmodifiableList(out);}
 public synchronized void reset(){emitted.clear();}public int[] getThresholds(){return thresholds.clone();}
}