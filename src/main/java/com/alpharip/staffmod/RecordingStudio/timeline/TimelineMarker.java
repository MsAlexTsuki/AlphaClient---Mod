package com.alpharip.staffmod.RecordingStudio.timeline;
/** Timestamped event or chapter marker. */
public final class TimelineMarker {private final long time;private final String label;private final TimelineMarkerKind kind;
 public TimelineMarker(long time,String label,TimelineMarkerKind kind){if(time<0||label==null||label.trim().isEmpty()||kind==null)throw new IllegalArgumentException();this.time=time;this.label=label.trim();this.kind=kind;}
 public long getTimeMillis(){return time;}public String getLabel(){return label;}public TimelineMarkerKind getKind(){return kind;}
}