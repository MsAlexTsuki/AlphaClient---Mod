package com.alpharip.staffmod.BuildTimelapse.capture;
/** Mutable UI settings; CaptureService snapshots these before queuing each frame. */
public final class CaptureSettings {
 private long interval=1000;private int width,height;private boolean hideHud=true,hideHand=true;
 public long getInterval(){return interval;}public void setInterval(long v){if(v<100||v>3600000)throw new IllegalArgumentException("Interval must be 100..3600000 ms");interval=v;}
 public void setResolution(int w,int h){if(w<0||h<0||(w==0)!=(h==0)||w>16384||h>16384)throw new IllegalArgumentException("Use native resolution (0,0) or a valid explicit size");width=w;height=h;}
 public int getWidth(){return width;}public int getHeight(){return height;}public boolean isHideHud(){return hideHud;}public void setHideHud(boolean v){hideHud=v;}public boolean isHideHand(){return hideHand;}public void setHideHand(boolean v){hideHand=v;}
 public CaptureSettings copy(){CaptureSettings c=new CaptureSettings();c.interval=interval;c.width=width;c.height=height;c.hideHud=hideHud;c.hideHand=hideHand;return c;}
}
