package com.alpharip.staffmod.RecordingStudio.timeline;
/** Reusable media clip; timeline positions and durations are milliseconds. */
public final class TimelineClip {
 private final String id,source;private long start,duration;private double speed=1;
 public TimelineClip(String id,String source,long start,long duration){if(blank(id)||blank(source)||start<0||duration<0)throw new IllegalArgumentException("clip");this.id=id.trim();this.source=source.trim();this.start=start;this.duration=duration;}
 public String getId(){return id;}public String getSource(){return source;}public synchronized long getStart(){return start;}public synchronized long getDuration(){return duration;}
 public synchronized long getEnd(){try{return Math.addExact(start,duration);}catch(ArithmeticException e){return Long.MAX_VALUE;}}
 public synchronized void setStart(long value){if(value<0)throw new IllegalArgumentException();start=value;}public synchronized void setDuration(long value){if(value<0)throw new IllegalArgumentException();duration=value;}
 public synchronized double getSpeed(){return speed;}public synchronized void setSpeed(double value){if(value<=0||value>100||Double.isNaN(value)||Double.isInfinite(value))throw new IllegalArgumentException();speed=value;}
 private static boolean blank(String value){return value==null||value.trim().isEmpty();}
}