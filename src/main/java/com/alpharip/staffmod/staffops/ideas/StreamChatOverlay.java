package com.alpharip.staffmod.staffops.ideas;

/** Foundation for configuring a read-only stream chat overlay. */
public class StreamChatOverlay {
 private boolean enabled; private int x,y; private float opacity=1.0f; private int maxMessages=8;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public void setPosition(int x,int y){this.x=x;this.y=y;} public int getX(){return x;} public int getY(){return y;}
 public float getOpacity(){return opacity;} public void setOpacity(float v){opacity=Math.max(0f,Math.min(1f,v));}
 public int getMaxMessages(){return maxMessages;} public void setMaxMessages(int v){maxMessages=Math.max(1,Math.min(50,v));}
}
