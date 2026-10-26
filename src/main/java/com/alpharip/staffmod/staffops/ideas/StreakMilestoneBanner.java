package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Emits milestone events when a caller reports kills; HUD rendering is not included in this model. */
public final class StreakMilestoneBanner {
 public static final String NAME="Streak Milestone Banner",CATEGORY="KitMap",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private final SortedSet<Integer> milestones=new TreeSet<Integer>(Arrays.asList(3,5,10,15,20));private int streak;
 public synchronized void setMilestones(Collection<Integer> values){if(values==null)throw new IllegalArgumentException("milestones required");TreeSet<Integer> next=new TreeSet<Integer>();for(Integer v:values){if(v==null||v<1)throw new IllegalArgumentException("milestones must be positive");next.add(v);}milestones.clear();milestones.addAll(next);}
 public synchronized List<Integer> recordKill(){int before=streak++;List<Integer> hit=new ArrayList<Integer>();for(Integer m:milestones)if(m==streak&&before<m)hit.add(m);return Collections.unmodifiableList(hit);}
 public synchronized void reset(){streak=0;}public synchronized int getStreak(){return streak;}
}