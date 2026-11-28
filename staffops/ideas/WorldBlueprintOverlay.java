package com.alpharip.staffmod.staffops.ideas;

/** Foundation for storing a world blueprint overlay configuration. */
public class WorldBlueprintOverlay {
 private boolean enabled; private String blueprintId=""; private float opacity=0.65f;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;} public String getBlueprintId(){return blueprintId;} public void setBlueprintId(String v){blueprintId=v==null?"":v;} public float getOpacity(){return opacity;} public void setOpacity(float v){opacity=Math.max(0.0f,Math.min(1.0f,v));}
}
