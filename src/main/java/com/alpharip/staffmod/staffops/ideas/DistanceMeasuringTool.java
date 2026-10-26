package com.alpharip.staffmod.staffops.ideas;

/** Foundation for measuring distances between two points. */
public class DistanceMeasuringTool {
 private double startX,startY,startZ,endX,endY,endZ; private boolean hasStart,hasEnd;
 public void setStart(double x,double y,double z){startX=x;startY=y;startZ=z;hasStart=true;} public void setEnd(double x,double y,double z){endX=x;endY=y;endZ=z;hasEnd=true;} public boolean isComplete(){return hasStart&&hasEnd;} public double getDistance(){if(!isComplete())return 0.0;double x=endX-startX,y=endY-startY,z=endZ-startZ;return Math.sqrt(x*x+y*y+z*z);}
}
