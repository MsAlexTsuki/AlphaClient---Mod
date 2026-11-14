package com.alpharip.staffmod.staffops.ideas;
/** HUD font preferences; resource loading and font renderer integration are not connected. */
public class CustomHudFont {
 private boolean enabled=true;private String resource="minecraft:default";private float scale=1F;private boolean shadow=true;
 public boolean isEnabled(){return enabled;}public void setEnabled(boolean v){enabled=v;}public String getResource(){return resource;}
 public void setResource(String r){if(r==null||r.trim().isEmpty())throw new IllegalArgumentException("Font resource required");resource=r.trim();}
 public float getScale(){return scale;}public void setScale(float s){if(Float.isNaN(s)||Float.isInfinite(s))throw new IllegalArgumentException("Invalid scale");scale=Math.max(.5F,Math.min(3F,s));}
 public boolean hasShadow(){return shadow;}public void setShadow(boolean v){shadow=v;}
}