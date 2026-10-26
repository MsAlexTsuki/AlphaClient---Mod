package com.alpharip.staffmod.staffops.ideas;

/** Foundation for in-game music playback preferences and track selection. */
public class InGameMusicPlayer {
 private boolean enabled; private float volume=0.5f; private String currentTrack="";
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public float getVolume(){return volume;} public void setVolume(float v){volume=Math.max(0f,Math.min(1f,v));}
 public String getCurrentTrack(){return currentTrack;} public void setCurrentTrack(String v){currentTrack=v==null?"":v;}
}
