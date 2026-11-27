package com.alpharip.staffmod.RecordingStudio.camera;
import java.util.*;
public final class CameraPath {
 private final String name;private final List<CameraWaypoint> points=new ArrayList<CameraWaypoint>();
 public CameraPath(String n){if(n==null||n.trim().isEmpty())throw new IllegalArgumentException("Path name is required");name=n.trim();}
 public String getName(){return name;}public synchronized void add(CameraWaypoint p){if(p==null)throw new IllegalArgumentException("Waypoint is required");for(CameraWaypoint existing:points)if(existing.getId().equals(p.getId()))throw new IllegalArgumentException("Duplicate waypoint id: "+p.getId());points.add(p);}
 public synchronized List<CameraWaypoint> getWaypoints(){return Collections.unmodifiableList(new ArrayList<CameraWaypoint>(points));}
 public synchronized long duration(){long total=0;for(int i=0;i<points.size();i++){CameraWaypoint p=points.get(i);total=safeAdd(total,p.getHold());if(i<points.size()-1)total=safeAdd(total,p.getDuration());}return total;}
 private static long safeAdd(long a,long b){try{return Math.addExact(a,b);}catch(ArithmeticException e){return Long.MAX_VALUE;}}
}