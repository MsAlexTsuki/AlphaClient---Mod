package com.alpharip.staffmod.staffops.ideas;

/** Foundation for exposing client statistics to an OBS overlay. */
public class ObsStatisticsOverlay {
 private boolean enabled; private int fps; private int pingMillis; private int kills; private int deaths;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public void update(int fpsValue,int ping,int killCount,int deathCount){fps=Math.max(0,fpsValue);pingMillis=Math.max(0,ping);kills=Math.max(0,killCount);deaths=Math.max(0,deathCount);}
 public int getFps(){return fps;} public int getPingMillis(){return pingMillis;} public int getKills(){return kills;} public int getDeaths(){return deaths;}
}
