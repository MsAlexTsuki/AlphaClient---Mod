package com.alpharip.staffmod.staffops.ideas;
/** Replay playback-speed state. Actual replay frame scheduling is not connected. */
public class ReplaySpeedController {
 private double speed=1.0;private boolean paused;
 public double getSpeed(){return speed;}public void setSpeed(double v){if(Double.isNaN(v)||Double.isInfinite(v))throw new IllegalArgumentException("Invalid speed");speed=Math.max(.1,Math.min(8,v));}
 public boolean isPaused(){return paused;}public void setPaused(boolean v){paused=v;}public void togglePaused(){paused=!paused;}
 public void reset(){speed=1.0;paused=false;}public String getSpeedLabel(){return String.format(java.util.Locale.ROOT,"%.2fx",speed);}
}