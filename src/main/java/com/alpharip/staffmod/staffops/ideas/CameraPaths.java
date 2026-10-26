package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Named camera paths with ordered 3D points. Playback/interpolation is not connected. */
public class CameraPaths {
 public static final class Point {public final double x,y,z,yaw,pitch;public final long holdMillis;public Point(double x,double y,double z,double yaw,double pitch,long hold){this.x=x;this.y=y;this.z=z;this.yaw=yaw;this.pitch=pitch;holdMillis=Math.max(0,hold);}}
 private final Map<String,List<Point>> paths=new LinkedHashMap<String,List<Point>>();
 public void addPoint(String name,Point p){if(name==null||name.trim().isEmpty()||p==null)throw new IllegalArgumentException("Path name and point required");String key=name.trim();List<Point> points=paths.get(key);if(points==null){points=new ArrayList<Point>();paths.put(key,points);}points.add(p);}
 public List<Point> getPoints(String name){List<Point> p=paths.get(name);return p==null?Collections.<Point>emptyList():Collections.unmodifiableList(new ArrayList<Point>(p));}
 public Set<String> getNames(){return Collections.unmodifiableSet(new LinkedHashSet<String>(paths.keySet()));}public boolean remove(String name){return paths.remove(name)!=null;}public void clearPoints(String name){List<Point> p=paths.get(name);if(p!=null)p.clear();}
}