package com.alpharip.staffmod.BuildTimelapse.camera;
import java.util.*;
public final class CameraPath {
 private final String name;private final List<CameraWaypoint> points=new ArrayList<CameraWaypoint>();
 public CameraPath(String n){if(n==null||n.trim().isEmpty())throw new IllegalArgumentException();name=n.trim();}
 public String getName(){return name;}public synchronized void add(CameraWaypoint p){if(p==null)throw new IllegalArgumentException();points.add(p);}
 public synchronized List<CameraWaypoint> getWaypoints(){return Collections.unmodifiableList(new ArrayList<CameraWaypoint>(points));}
 /** Each waypoint holds at its pose, then spends its duration moving to the next waypoint. */
 public synchronized long duration(){long d=0;for(int i=0;i<points.size();i++){CameraWaypoint p=points.get(i);d+=p.getHold();if(i<points.size()-1)d+=p.getDuration();}return d;}
}
