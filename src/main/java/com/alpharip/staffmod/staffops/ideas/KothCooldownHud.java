package com.alpharip.staffmod.staffops.ideas;
/** KOTH cooldown state; chat parsing and HUD rendering are not wired. */
public class KothCooldownHud {private long readyAt=-1;private boolean enabled=true;
 public void setCooldown(long now,long duration){if(now<0||duration<0)throw new IllegalArgumentException();readyAt=now+duration;}public long remainingMillis(long now){return readyAt<0?0:Math.max(0,readyAt-now);}public boolean isReady(long now){return readyAt<0||now>=readyAt;}public boolean isEnabled(){return enabled;}public void setEnabled(boolean v){enabled=v;}public void clear(){readyAt=-1;}
}