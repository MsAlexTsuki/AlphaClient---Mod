package com.alpharip.staffmod.staffops.ideas;

/** Foundation for an FPS cap applied while the game window is unfocused. */
public class BackgroundFPSLimit {
 private boolean enabled=true; private int fps=30;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public int getFps(){return fps;} public void setFps(int v){fps=Math.max(1,Math.min(240,v));}
}
