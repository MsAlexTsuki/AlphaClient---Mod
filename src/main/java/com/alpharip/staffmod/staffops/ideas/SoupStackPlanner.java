package com.alpharip.staffmod.staffops.ideas;
import java.util.ArrayDeque;
import java.util.Deque;
/** Estimates soup stacks from recent observed consumption. Runtime inventory/chat hooks are separate integration work. */
public final class SoupStackPlanner {
 public static final String NAME="Soup Stack Planner", CATEGORY="KitMap", STATUS="NEW", REQUIRED_ACCESS="Unconfirmed";
 private final Deque<Integer> recent=new ArrayDeque<Integer>(); private final int sampleLimit;
 public SoupStackPlanner(){this(10);} public SoupStackPlanner(int sampleLimit){if(sampleLimit<1)throw new IllegalArgumentException("sampleLimit must be positive");this.sampleLimit=sampleLimit;}
 public synchronized void recordSoupConsumed(int count){if(count<0)throw new IllegalArgumentException("count must not be negative");recent.addLast(count);while(recent.size()>sampleLimit)recent.removeFirst();}
 public synchronized double getAverageConsumed(){if(recent.isEmpty())return 0;long total=0;for(int n:recent)total+=n;return (double)total/recent.size();}
 public synchronized int recommendSoups(int expectedFights,double safetyFactor){if(expectedFights<0||safetyFactor<1.0)throw new IllegalArgumentException("invalid fight count or safety factor");return (int)Math.ceil(getAverageConsumed()*expectedFights*safetyFactor);}
 public synchronized int recommendStacks(int expectedFights,int soupsPerStack,double safetyFactor){if(soupsPerStack<1)throw new IllegalArgumentException("soupsPerStack must be positive");return (int)Math.ceil((double)recommendSoups(expectedFights,safetyFactor)/soupsPerStack);}
 public synchronized void reset(){recent.clear();}
}