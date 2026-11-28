package com.alpharip.staffmod.staffops.ideas;

/** Foundation for screenshot watermark settings. */
public class ScreenshotWatermark {
 private boolean enabled; private String text="AlphaClient"; private float opacity=0.75f;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public String getText(){return text;} public void setText(String v){text=v==null?"":v;}
 public float getOpacity(){return opacity;} public void setOpacity(float v){opacity=Math.max(0.0f,Math.min(1.0f,v));}
}
