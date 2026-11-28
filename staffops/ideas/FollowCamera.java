package com.alpharip.staffmod.staffops.ideas;
/** Follow-camera preferences. Tracking a live Minecraft entity is not connected. */
public class FollowCamera {
 public enum Mode { BEHIND, FRONT, SIDE, ORBIT }
 private boolean enabled=true,smooth=true;private Mode mode=Mode.BEHIND;private double distance=4.0,height=1.5,smoothing=.25;private String target="";
 public boolean isEnabled(){return enabled;}public void setEnabled(boolean v){enabled=v;}public String getTarget(){return target;}public void setTarget(String s){target=s==null?"":s.trim();}
 public Mode getMode(){return mode;}public void setMode(Mode m){if(m==null)throw new IllegalArgumentException("Mode required");mode=m;}
 public double getDistance(){return distance;}public void setDistance(double v){if(Double.isNaN(v)||Double.isInfinite(v))throw new IllegalArgumentException("Invalid distance");distance=Math.max(1,Math.min(16,v));}
 public double getHeight(){return height;}public void setHeight(double v){if(Double.isNaN(v)||Double.isInfinite(v))throw new IllegalArgumentException("Invalid height");height=Math.max(-4,Math.min(8,v));}
 public boolean isSmooth(){return smooth;}public void setSmooth(boolean v){smooth=v;}public double getSmoothing(){return smoothing;}public void setSmoothing(double v){if(Double.isNaN(v)||Double.isInfinite(v))throw new IllegalArgumentException("Invalid smoothing");smoothing=Math.max(.01,Math.min(1,v));}
}