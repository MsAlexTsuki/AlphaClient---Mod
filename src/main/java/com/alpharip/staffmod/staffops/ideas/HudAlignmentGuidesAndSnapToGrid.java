package com.alpharip.staffmod.staffops.ideas;

/** Foundation for HUD alignment guides and grid snapping. */
public class HudAlignmentGuidesAndSnapToGrid {
 private boolean guidesEnabled=true,snapEnabled=true; private int gridSize=5;
 public boolean areGuidesEnabled(){return guidesEnabled;} public void setGuidesEnabled(boolean v){guidesEnabled=v;}
 public boolean isSnapEnabled(){return snapEnabled;} public void setSnapEnabled(boolean v){snapEnabled=v;}
 public int getGridSize(){return gridSize;} public void setGridSize(int v){gridSize=Math.max(1,Math.min(100,v));}
 public int snap(int coordinate){return snapEnabled?Math.round((float)coordinate/gridSize)*gridSize:coordinate;}
}
