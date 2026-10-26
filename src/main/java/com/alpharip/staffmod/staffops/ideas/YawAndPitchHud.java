package com.alpharip.staffmod.staffops.ideas;

/** Foundation for storing player yaw and pitch HUD visibility and values. */
public class YawAndPitchHud {
 private boolean enabled=true; private float yaw,pitch;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public void update(float yaw,float pitch){this.yaw=yaw;this.pitch=pitch;}
 public float getYaw(){return yaw;} public float getPitch(){return pitch;}
 public String format(){return String.format(java.util.Locale.ROOT,"Yaw: %.1f  Pitch: %.1f",yaw,pitch);}
}
