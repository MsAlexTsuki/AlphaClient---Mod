package com.alpharip.staffmod.staffops.ideas;
/** Records timing between block actions; Minecraft input/render hooks are not connected. */
public class BlockTimingIndicator {private long last=-1,interval=-1;private int samples;private boolean enabled=true;
 public void recordAction(long millis){if(millis<0||last>=0&&millis<last)throw new IllegalArgumentException("Timestamp must be monotonic.");if(last>=0){interval=millis-last;samples++;}last=millis;}
 public long getLastIntervalMillis(){return interval;}public int getSamples(){return samples;}public boolean isEnabled(){return enabled;}public void setEnabled(boolean v){enabled=v;}public void reset(){last=-1;interval=-1;samples=0;}
}