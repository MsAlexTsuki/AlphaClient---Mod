package com.alpharip.staffmod.staffops.ideas;
/** Timed HUD visibility state; call isHidden from the actual render/event loop. */
public class TemporaryHudHideShortcut {
 private boolean enabled=true,hidden;private long hiddenUntil;public boolean isEnabled(){return enabled;}public void setEnabled(boolean v){enabled=v;if(!v)hidden=false;}
 public void hideFor(long now,long duration){if(!enabled)return;if(duration<=0)throw new IllegalArgumentException("Duration must be positive");hidden=true;hiddenUntil=now+duration;}
 public boolean isHidden(long now){if(hidden&&now>=hiddenUntil)hidden=false;return enabled&&hidden;}public long getRemainingMillis(long now){return isHidden(now)?Math.max(0,hiddenUntil-now):0;}public void show(){hidden=false;}
}