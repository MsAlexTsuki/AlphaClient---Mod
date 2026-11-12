package com.alpharip.staffmod.staffops.ideas;

/** Foundation for mouse-trail visual preferences and pointer samples. */
public class MouseTrail {
 public static final class Point { public final int x,y; public final long timestamp; public Point(int x,int y,long t){this.x=x;this.y=y;timestamp=t;} }
 private boolean enabled; private int maxPoints=12; private final java.util.Deque<Point> points=new java.util.ArrayDeque<Point>();
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public int getMaxPoints(){return maxPoints;} public void setMaxPoints(int v){maxPoints=Math.max(1,Math.min(100,v));trim();}
 public void addPoint(int x,int y,long timestamp){if(!enabled)return;points.addLast(new Point(x,y,timestamp));trim();}
 private void trim(){while(points.size()>maxPoints)points.removeFirst();}
 public java.util.List<Point> getPoints(){return java.util.Collections.unmodifiableList(new java.util.ArrayList<Point>(points));}
 public void clear(){points.clear();}
}
