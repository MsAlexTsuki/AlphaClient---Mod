package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Optional local mood journal; no queue blocking or external upload is performed. */
public final class QueueMoodCheck {
 public static final String NAME="Queue Mood Check",CATEGORY="Practice",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 public static final class CheckIn {public final int mood;public final String note;public final long time;private CheckIn(int m,String n){mood=m;note=n;time=System.currentTimeMillis();}}
 private final List<CheckIn> history=new ArrayList<CheckIn>();
 public synchronized CheckIn record(int mood,String note){if(mood<1||mood>5)throw new IllegalArgumentException("mood must be 1..5");CheckIn c=new CheckIn(mood,note==null?"":note.trim());history.add(c);return c;}
 public synchronized List<CheckIn> history(){return Collections.unmodifiableList(new ArrayList<CheckIn>(history));}
 public synchronized double averageMood(){if(history.isEmpty())return 0;int s=0;for(CheckIn c:history)s+=c.mood;return (double)s/history.size();}
 public synchronized void clear(){history.clear();}
}