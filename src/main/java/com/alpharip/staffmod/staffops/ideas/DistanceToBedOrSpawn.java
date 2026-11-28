package com.alpharip.staffmod.staffops.ideas;

/** Foundation for distance calculations to a saved bed or world spawn. */
public class DistanceToBedOrSpawn {
 private double targetX,targetY,targetZ; private String targetType="spawn"; private boolean targetSet;
 public void setTarget(String type,double x,double y,double z){targetType=type==null?"spawn":type;targetX=x;targetY=y;targetZ=z;targetSet=true;}
 public boolean hasTarget(){return targetSet;} public String getTargetType(){return targetType;}
 public double distanceTo(double x,double y,double z){double dx=x-targetX,dy=y-targetY,dz=z-targetZ;return Math.sqrt(dx*dx+dy*dy+dz*dz);}
 public double horizontalDistanceTo(double x,double z){double dx=x-targetX,dz=z-targetZ;return Math.sqrt(dx*dx+dz*dz);}
}
