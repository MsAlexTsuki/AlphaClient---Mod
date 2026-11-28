package com.alpharip.staffmod.staffops.ideas;
/** Determines when an idle screen saver should activate and when activity resets it. */
public final class StarfallScreenSaver {
 public static final String NAME="Starfall Screen Saver",CATEGORY="FounderFun",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private long idleThreshold=60000,lastActivity;
 private boolean enabled=true,active;
 public synchronized void setIdleThreshold(long ms){if(ms<1000)throw new IllegalArgumentException("threshold must be at least one second");idleThreshold=ms;}
 public synchronized void setEnabled(boolean v){enabled=v;if(!v)active=false;}
 public synchronized void activity(long now){checkTime(now);lastActivity=now;active=false;}
 public synchronized boolean update(long now){checkTime(now);active=enabled&&now-lastActivity>=idleThreshold;return active;}
 public synchronized boolean isActive(){return active;}
 private static void checkTime(long t){if(t<0)throw new IllegalArgumentException("time must be non-negative");}
}