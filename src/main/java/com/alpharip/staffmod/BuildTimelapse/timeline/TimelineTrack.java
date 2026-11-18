package com.alpharip.staffmod.BuildTimelapse.timeline;
import java.util.*;
public final class TimelineTrack {
 private final String id,name;private final List<TimelineClip> clips=new ArrayList<TimelineClip>();
 public TimelineTrack(String id,String name){if(blank(id)||blank(name))throw new IllegalArgumentException("Track id and name are required");this.id=id.trim();this.name=name.trim();}
 public String getId(){return id;}public String getName(){return name;}
 public synchronized void add(TimelineClip c){if(c==null)throw new IllegalArgumentException();for(TimelineClip existing:clips)if(existing.getId().equals(c.getId()))throw new IllegalArgumentException("Duplicate clip id: "+c.getId());clips.add(c);sort();}
 private void sort(){Collections.sort(clips,new Comparator<TimelineClip>(){public int compare(TimelineClip a,TimelineClip b){return Long.compare(a.getStart(),b.getStart());}});}
 public synchronized List<TimelineClip> getClips(){sort();return Collections.unmodifiableList(new ArrayList<TimelineClip>(clips));}
 public synchronized boolean remove(String clipId){for(Iterator<TimelineClip> i=clips.iterator();i.hasNext();)if(i.next().getId().equals(clipId)){i.remove();return true;}return false;}
 private static boolean blank(String s){return s==null||s.trim().isEmpty();}
}