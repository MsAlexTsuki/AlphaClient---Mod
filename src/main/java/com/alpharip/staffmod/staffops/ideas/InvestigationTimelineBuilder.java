package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Builds a sorted view of caller-supplied investigation events. */
public final class InvestigationTimelineBuilder {
 private final List<Event> events=new ArrayList<Event>();
 public void add(String id,long timestampMillis,String type,String summary){events.add(new Event(id,timestampMillis,type,summary));}
 public List<Event> build(){List<Event> result=new ArrayList<Event>(events);Collections.sort(result,new Comparator<Event>(){public int compare(Event a,Event b){return Long.compare(a.timestampMillis,b.timestampMillis);}});return Collections.unmodifiableList(result);}
 public void clear(){events.clear();}
 public static final class Event {private final String id,type,summary;private final long timestampMillis;private Event(String id,long time,String type,String summary){this.id=id==null?"":id;this.timestampMillis=time;this.type=type==null?"":type;this.summary=summary==null?"":summary;}public String getId(){return id;}public long getTimestampMillis(){return timestampMillis;}public String getType(){return type;}public String getSummary(){return summary;}}
}