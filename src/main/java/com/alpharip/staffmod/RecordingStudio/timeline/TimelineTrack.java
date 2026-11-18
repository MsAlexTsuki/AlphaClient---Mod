package com.alpharip.staffmod.RecordingStudio.timeline;
import java.util.*;
/** Ordered collection of clips on a reusable media track. */
public final class TimelineTrack {
 private final String id,name;private final List<TimelineClip> clips=new ArrayList<TimelineClip>();
 public TimelineTrack(String id,String name){if(blank(id)||blank(name))throw new IllegalArgumentException("Track id and name are required");this.id=id.trim();this.name=name.trim();}
 public String getId(){return id;}public String getName(){return name;}
 public synchronized void add(TimelineClip clip){if(clip==null)throw new IllegalArgumentException();for(TimelineClip existing:clips)if(existing.getId().equals(clip.getId()))throw new IllegalArgumentException("Duplicate clip id: "+clip.getId());clips.add(clip);sort();}
 public synchronized boolean remove(String id){for(Iterator<TimelineClip> it=clips.iterator();it.hasNext();)if(it.next().getId().equals(id)){it.remove();return true;}return false;}
 public synchronized boolean move(String id,long start){TimelineClip clip=find(id);if(clip==null)return false;clip.setStart(start);sort();return true;}
 public synchronized boolean trim(String id,long start,long duration){TimelineClip clip=find(id);if(clip==null)return false;clip.setStart(start);clip.setDuration(duration);sort();return true;}
 public synchronized boolean setSpeed(String id,double speed){TimelineClip clip=find(id);if(clip==null)return false;clip.setSpeed(speed);return true;}
 public synchronized TimelineClip find(String id){for(TimelineClip clip:clips)if(clip.getId().equals(id))return clip;return null;}
 private void sort(){Collections.sort(clips,new Comparator<TimelineClip>(){public int compare(TimelineClip a,TimelineClip b){return Long.compare(a.getStart(),b.getStart());}});}
 public synchronized List<TimelineClip> getClips(){sort();return Collections.unmodifiableList(new ArrayList<TimelineClip>(clips));}
 private static boolean blank(String value){return value==null||value.trim().isEmpty();}
}