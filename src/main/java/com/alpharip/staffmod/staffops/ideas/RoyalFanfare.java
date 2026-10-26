package com.alpharip.staffmod.staffops.ideas;
/** Controls login-jingle preference and prevents repeated playback within a cooldown. */
public final class RoyalFanfare {
 public static final String NAME="Royal Fanfare",CATEGORY="OwnerFun",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private boolean enabled=true; private long cooldownMillis=5000,lastPlayed=-1;
 public synchronized void setEnabled(boolean v){enabled=v;} public synchronized boolean isEnabled(){return enabled;}
 public synchronized void setCooldownMillis(long v){if(v<0)throw new IllegalArgumentException("cooldown must be non-negative");cooldownMillis=v;}
 public synchronized boolean shouldPlay(long now){if(now<0)throw new IllegalArgumentException("time must be non-negative");if(!enabled||(lastPlayed>=0&&now-lastPlayed<cooldownMillis))return false;lastPlayed=now;return true;}
}