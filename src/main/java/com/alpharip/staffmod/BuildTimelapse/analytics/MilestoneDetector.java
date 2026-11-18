package com.alpharip.staffmod.BuildTimelapse.analytics;
import java.util.*;
public final class MilestoneDetector {
 private final int[] thresholds;private final Set<Integer> emitted=new HashSet<Integer>();
 public MilestoneDetector(int... t){if(t==null){thresholds=new int[0];return;}for(int n:t)if(n<=0)throw new IllegalArgumentException("Milestone thresholds must be positive");thresholds=t.clone();Arrays.sort(thresholds);}
 public synchronized List<String> update(long placements){List<String> out=new ArrayList<String>();for(int n:thresholds)if(placements>=n&&emitted.add(n))out.add(n+" blocks placed");return Collections.unmodifiableList(out);}
 public synchronized void reset(){emitted.clear();}
}