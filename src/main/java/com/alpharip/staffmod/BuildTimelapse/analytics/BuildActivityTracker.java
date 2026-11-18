package com.alpharip.staffmod.BuildTimelapse.analytics;
import java.util.*;
public final class BuildActivityTracker {
 private long placed,removed,last;private final Deque<BlockChangeEvent> events=new ArrayDeque<BlockChangeEvent>();
 public synchronized void record(BlockChangeEvent e){if(e==null||!e.isChange())return;BlockChangeEvent p=events.peekLast();if(p!=null&&p.x==e.x&&p.y==e.y&&p.z==e.z&&p.time==e.time&&p.before.equalsIgnoreCase(e.before)&&p.after.equalsIgnoreCase(e.after))return;events.addLast(e);if(e.isPlacement())placed++;if(e.isRemoval())removed++;last=e.time;while(events.size()>10000)events.removeFirst();}
 public synchronized long getPlaced(){return placed;}public synchronized long getRemoved(){return removed;}public synchronized long getLastActivity(){return last;}
 public synchronized List<BlockChangeEvent> snapshot(){return Collections.unmodifiableList(new ArrayList<BlockChangeEvent>(events));}public synchronized void reset(){placed=removed=last=0;events.clear();}
}