package com.alpharip.staffmod.staffops.ideas;
/** Free-camera state for replay viewing. Replay timeline and camera hooks are not connected. */
public class ReplayFreecam {
 private boolean active;private double x,y,z,yaw,pitch,speed=1;
 public boolean isActive(){return active;}public void setActive(boolean v){active=v;}public void setPosition(double x,double y,double z){this.x=x;this.y=y;this.z=z;}
 public double getX(){return x;}public double getY(){return y;}public double getZ(){return z;}
 public void setRotation(double yaw,double pitch){this.yaw=yaw;this.pitch=Math.max(-90,Math.min(90,pitch));}public double getYaw(){return yaw;}public double getPitch(){return pitch;}
 public double getSpeed(){return speed;}public void setSpeed(double v){if(Double.isNaN(v)||Double.isInfinite(v))throw new IllegalArgumentException("Invalid speed");speed=Math.max(.1,Math.min(20,v));}
}