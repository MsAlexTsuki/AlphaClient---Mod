package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Timestamp-based reaction trials; UI signal scheduling is separate. */
public class ReactionTimeTest {
 private long signal=-1;private boolean falseStart;private final List<Long> results=new ArrayList<Long>();
 public void startSignal(long nanos){if(nanos<0)throw new IllegalArgumentException();signal=nanos;falseStart=false;}
 public long react(long nanos){if(signal<0)throw new IllegalStateException("No active signal");if(nanos<signal){falseStart=true;return -1;}long ms=(nanos-signal)/1000000L;results.add(ms);signal=-1;return ms;}
 public void registerPrematureInput(){if(signal>=0)falseStart=true;}
 public boolean hadFalseStart(){return falseStart;}public List<Long> getResults(){return Collections.unmodifiableList(new ArrayList<Long>(results));}
 public double averageMillis(){if(results.isEmpty())return 0;long sum=0;for(Long x:results)sum+=x;return (double)sum/results.size();}
 public void reset(){signal=-1;falseStart=false;results.clear();}
}