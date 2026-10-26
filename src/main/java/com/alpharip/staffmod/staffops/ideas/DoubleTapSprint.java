package com.alpharip.staffmod.staffops.ideas;

/** Foundation for configurable double-tap sprint input detection. */
public class DoubleTapSprint {
 private boolean enabled=true; private long doubleTapWindowMillis=250L,lastForwardPress=-1L;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public long getDoubleTapWindowMillis(){return doubleTapWindowMillis;} public void setDoubleTapWindowMillis(long v){doubleTapWindowMillis=Math.max(100L,Math.min(750L,v));}
 public boolean onForwardPressed(long nowMillis){boolean triggered=enabled&&lastForwardPress>=0L&&nowMillis>=lastForwardPress&&nowMillis-lastForwardPress<=doubleTapWindowMillis;lastForwardPress=nowMillis;return triggered;}
 public void reset(){lastForwardPress=-1L;}
}
