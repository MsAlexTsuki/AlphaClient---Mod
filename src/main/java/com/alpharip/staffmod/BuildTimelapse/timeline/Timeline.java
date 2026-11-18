package com.alpharip.staffmod.BuildTimelapse.timeline;
import java.util.*;
public final class Timeline {
 private final List<TimelineMarker> markers=new ArrayList<TimelineMarker>();private final List<TimelineTrack> tracks=new ArrayList<TimelineTrack>();
 public synchronized void addMarker(TimelineMarker m){if(m==null)throw new IllegalArgumentException();markers.add(m);Collections.sort(markers,new Comparator<TimelineMarker>(){public int compare(TimelineMarker a,TimelineMarker b){return Long.compare(a.getTimeMillis(),b.getTimeMillis());}});}
 public synchronized void addTrack(TimelineTrack t){if(t==null)throw new IllegalArgumentException();for(TimelineTrack existing:tracks)if(existing.getId().equals(t.getId()))throw new IllegalArgumentException("Duplicate track id: "+t.getId());tracks.add(t);}
 public synchronized List<TimelineMarker> getMarkers(){return Collections.unmodifiableList(new ArrayList<TimelineMarker>(markers));}public synchronized List<TimelineTrack> getTracks(){return Collections.unmodifiableList(new ArrayList<TimelineTrack>(tracks));}
 public synchronized long duration(){long d=markers.isEmpty()?0:markers.get(markers.size()-1).getTimeMillis();for(TimelineTrack t:tracks)for(TimelineClip c:t.getClips())d=Math.max(d,c.getEnd());return d;}
}