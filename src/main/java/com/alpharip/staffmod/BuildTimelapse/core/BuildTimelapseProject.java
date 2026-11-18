package com.alpharip.staffmod.BuildTimelapse.core;
import java.util.UUID;
public final class BuildTimelapseProject {private final String id;private String name,world,notes="";private final long created;private long started,ended,pausedAt,pausedTotal;private boolean recording;
 public BuildTimelapseProject(String n,String w){this(UUID.randomUUID().toString(),n,w,System.currentTimeMillis());}
 public BuildTimelapseProject(String id,String n,String w,long c){if(blank(id)||blank(n))throw new IllegalArgumentException("id/name");this.id=id;name=n.trim();world=w==null?"":w;created=c;}
 public synchronized void start(long now){if(!recording){recording=true;if(started==0)started=now;ended=0;}}public synchronized void pause(long now){if(recording){recording=false;pausedAt=now;}}
 public synchronized void resume(long now){if(!recording){if(pausedAt>0)pausedTotal+=Math.max(0,now-pausedAt);pausedAt=0;recording=true;}}public synchronized void stop(long now){if(recording){recording=false;ended=now;}else if(ended==0)ended=now;}
 public String getId(){return id;}public synchronized String getName(){return name;}public synchronized void setName(String v){if(blank(v))throw new IllegalArgumentException("name");name=v.trim();}
 public synchronized String getWorld(){return world;}public synchronized void setWorld(String v){world=v==null?"":v;}public synchronized String getNotes(){return notes;}public synchronized void setNotes(String v){notes=v==null?"":v;}
 public long getCreated(){return created;}public synchronized long getStarted(){return started;}public synchronized long getEnded(){return ended;}public synchronized boolean isRecording(){return recording;}
 public synchronized long duration(long now){if(started==0)return 0;long e=recording?now:(ended>0?ended:(pausedAt>0?pausedAt:now));return Math.max(0,e-started-pausedTotal);}private static boolean blank(String s){return s==null||s.trim().isEmpty();}
}
