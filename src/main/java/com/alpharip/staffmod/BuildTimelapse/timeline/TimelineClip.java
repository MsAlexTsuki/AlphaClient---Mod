package com.alpharip.staffmod.BuildTimelapse.timeline;
public final class TimelineClip {
 private final String id,source;private long start,duration;private double speed=1;
 public TimelineClip(String id,String source,long start,long duration){if(blank(id)||blank(source)||start<0||duration<0)throw new IllegalArgumentException("clip");this.id=id.trim();this.source=source.trim();this.start=start;this.duration=duration;}
 public String getId(){return id;}public String getSource(){return source;}public synchronized long getStart(){return start;}public synchronized long getDuration(){return duration;}public synchronized long getEnd(){try{return Math.addExact(start,duration);}catch(ArithmeticException e){return Long.MAX_VALUE;}}
 public synchronized void setStart(long v){if(v<0)throw new IllegalArgumentException();start=v;}public synchronized void setDuration(long v){if(v<0)throw new IllegalArgumentException();duration=v;}
 public synchronized double getSpeed(){return speed;}public synchronized void setSpeed(double v){if(v<=0||v>100||Double.isNaN(v)||Double.isInfinite(v))throw new IllegalArgumentException();speed=v;}
 private static boolean blank(String s){return s==null||s.trim().isEmpty();}
}