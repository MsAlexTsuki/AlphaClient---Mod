package com.alpharip.staffmod.staffops.ideas;
/** Free-camera control preferences. Camera entity/render integration is not connected. */
public class FreeCamera {
 private boolean enabled, collision=true, pausePlayer=true;private double speed=1.0;
 public boolean isEnabled(){return enabled;}public void setEnabled(boolean v){enabled=v;}
 public boolean isCollisionEnabled(){return collision;}public void setCollisionEnabled(boolean v){collision=v;}
 public boolean pausesPlayer(){return pausePlayer;}public void setPausePlayer(boolean v){pausePlayer=v;}
 public double getSpeed(){return speed;}public void setSpeed(double v){if(Double.isNaN(v)||Double.isInfinite(v))throw new IllegalArgumentException("Invalid speed");speed=Math.max(.1,Math.min(10,v));}
}