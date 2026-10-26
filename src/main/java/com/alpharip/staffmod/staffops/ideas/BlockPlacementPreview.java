package com.alpharip.staffmod.staffops.ideas;

/** Foundation for block placement preview settings. */
public class BlockPlacementPreview {
 private boolean enabled; private boolean showCollision; private float opacity=0.5f;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;} public boolean isShowCollision(){return showCollision;} public void setShowCollision(boolean v){showCollision=v;} public float getOpacity(){return opacity;} public void setOpacity(float v){opacity=Math.max(0.0f,Math.min(1.0f,v));}
}
