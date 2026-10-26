package com.alpharip.staffmod.staffops.ideas;
/** Stores a player-selected safe point and computes a navigation bearing. Does not render an in-game pointer yet. */
public final class SafePointPointer {
 public static final String NAME="Safe Point Pointer", CATEGORY="KitMap", STATUS="NEW", REQUIRED_ACCESS="Unconfirmed";
 public static final class Point {public final String world;public final int x,y,z;public Point(String world,int x,int y,int z){if(world==null||world.trim().isEmpty())throw new IllegalArgumentException("world is required");this.world=world;this.x=x;this.y=y;this.z=z;}}
 private Point target;
 public synchronized void setTarget(Point point){if(point==null)throw new IllegalArgumentException("point is required");target=point;}
 public synchronized Point getTarget(){return target;} public synchronized void clear(){target=null;}
 public synchronized double bearingDegrees(double playerX,double playerZ,String world){if(target==null)throw new IllegalStateException("No safe point selected");if(!target.world.equals(world))throw new IllegalArgumentException("Safe point belongs to another world");double angle=Math.toDegrees(Math.atan2(target.z-playerZ,target.x-playerX));return (angle+360.0)%360.0;}
 public synchronized double distance(double playerX,double playerY,double playerZ,String world){if(target==null)throw new IllegalStateException("No safe point selected");if(!target.world.equals(world))throw new IllegalArgumentException("Safe point belongs to another world");double dx=target.x-playerX,dy=target.y-playerY,dz=target.z-playerZ;return Math.sqrt(dx*dx+dy*dy+dz*dz);}
}