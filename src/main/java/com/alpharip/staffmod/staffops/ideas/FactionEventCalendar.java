package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Local faction event schedule. */
public final class FactionEventCalendar {
 public static final String NAME="Faction Event Calendar",CATEGORY="HardcoreFactions",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 public static final class Event {public final String id,title,notes;public final long startsAt;private Event(String i,String t,long s,String n){id=i;title=t;startsAt=s;notes=n;}}
 private final Map<String,Event> events=new LinkedHashMap<String,Event>();
 public synchronized void put(String id,String title,long startsAt,String notes){if(startsAt<0)throw new IllegalArgumentException("start time must be non-negative");String i=text(id);events.put(i,new Event(i,text(title),startsAt,notes==null?"":notes.trim()));}
 public synchronized List<Event> upcoming(long now){List<Event> out=new ArrayList<Event>();for(Event e:events.values())if(e.startsAt>=now)out.add(e);Collections.sort(out,new Comparator<Event>(){public int compare(Event a,Event b){return Long.compare(a.startsAt,b.startsAt);}});return Collections.unmodifiableList(out);}
 public synchronized boolean remove(String id){return events.remove(text(id))!=null;}
 private static String text(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}