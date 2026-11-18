package com.alpharip.staffmod.RecordingStudio.timeline;
import java.util.*;
/** General-purpose timeline shared by Recording Studio and feature-specific recording modules. */
public final class Timeline {
 private final List<TimelineMarker> markers=new ArrayList<TimelineMarker>(); private final List<TimelineTrack> tracks=new ArrayList<TimelineTrack>();
 public synchronized void addMarker(TimelineMarker marker){if(marker==null)throw new IllegalArgumentException();markers.add(marker);Collections.sort(markers,new Comparator<TimelineMarker>(){public int compare(TimelineMarker a,TimelineMarker b){return Long.compare(a.getTimeMillis(),b.getTimeMillis());}});}
 public synchronized void addTrack(TimelineTrack track){if(track==null)throw new IllegalArgumentException();for(TimelineTrack existing:tracks)if(existing.getId().equals(track.getId()))throw new IllegalArgumentException("Duplicate track id: "+track.getId());tracks.add(track);}
 public synchronized List<TimelineMarker> getMarkers(){return Collections.unmodifiableList(new ArrayList<TimelineMarker>(markers));}
 public synchronized List<TimelineTrack> getTracks(){return Collections.unmodifiableList(new ArrayList<TimelineTrack>(tracks));}
 public synchronized long duration(){long duration=markers.isEmpty()?0:markers.get(markers.size()-1).getTimeMillis();for(TimelineTrack track:tracks)for(TimelineClip clip:track.getClips())duration=Math.max(duration,clip.getEnd());return duration;}
}