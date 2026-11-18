package com.alpharip.staffmod.BuildTimelapse.timeline;
import java.util.*;
public final class TimelineTrack {
 private final String id,name;private final List<TimelineClip> clips=new ArrayList<TimelineClip>();
 public TimelineTrack(String id,String name){if(blank(id)||blank(name))throw new IllegalArgumentException("Track id and name are required");this.id=id.trim();this.name=name.trim();}
 public String getId(){return id;}public String getName(){return name;}
 public synchronized void add(TimelineClip c){if(c==null)throw new IllegalArgumentException();for(TimelineClip e:clips)if(e.getId().equals(c.getId()))throw new IllegalArgumentException("Duplicate clip id: "+c.getId());clips.add(c);sort();}
 public synchronized boolean remove(String id){for(Iterator<TimelineClip> i=clips.iterator();i.hasNext();)if(i.next().getId().equals(id)){i.remove();return true;}return false;}
 public synchronized boolean move(String id,long start){TimelineClip c=find(id);if(c==null)return false;c.setStart(start);sort();return true;}
 public synchronized boolean trim(String id,long start,long duration){TimelineClip c=find(id);if(c==null)return false;c.setStart(start);c.setDuration(duration);sort();return true;}
 public synchronized boolean setSpeed(String id,double speed){TimelineClip c=find(id);if(c==null)return false;c.setSpeed(speed);return true;}
 public synchronized TimelineClip find(String id){for(TimelineClip c:clips)if(c.getId().equals(id))return c;return null;}
 private void sort(){Collections.sort(clips,new Comparator<TimelineClip>(){public int compare(TimelineClip a,TimelineClip b){return Long.compare(a.getStart(),b.getStart());}});}
 public synchronized List<TimelineClip> getClips(){sort();return Collections.unmodifiableList(new ArrayList<TimelineClip>(clips));}
 private static boolean blank(String s){return s==null||s.trim().isEmpty();}
}
