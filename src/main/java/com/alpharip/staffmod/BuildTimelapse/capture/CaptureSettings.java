package com.alpharip.staffmod.BuildTimelapse.capture;
/** Mutable UI settings; CaptureService snapshots these before queuing each frame. */
public final class CaptureSettings {
 private long interval=1000;private int width,height;private boolean hideHud=true,hideHand=true;
 public synchronized long getInterval(){return interval;}public synchronized void setInterval(long v){if(v<100||v>3600000)throw new IllegalArgumentException("Interval must be 100..3600000 ms");interval=v;}
 public synchronized void setResolution(int w,int h){if(w<0||h<0||(w==0)!=(h==0)||(w!=0&&(w<16||h<16))||w>16384||h>16384)throw new IllegalArgumentException("Use native resolution (0,0) or a valid size of at least 16x16");width=w;height=h;}
 public synchronized int getWidth(){return width;}public synchronized int getHeight(){return height;}public synchronized boolean isHideHud(){return hideHud;}public synchronized void setHideHud(boolean v){hideHud=v;}public synchronized boolean isHideHand(){return hideHand;}public synchronized void setHideHand(boolean v){hideHand=v;}
 public synchronized CaptureSettings copy(){CaptureSettings c=new CaptureSettings();c.interval=interval;c.width=width;c.height=height;c.hideHud=hideHud;c.hideHand=hideHand;return c;}
}