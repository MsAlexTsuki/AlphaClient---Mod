package com.alpharip.staffmod.staffops.ideas;

/** Foundation for builder grid and ruler overlay configuration. */
public class BuilderGridAndRulerOverlay {
 private boolean gridEnabled=true,rulerEnabled=true; private int gridSpacing=1;
 public boolean isGridEnabled(){return gridEnabled;} public void setGridEnabled(boolean v){gridEnabled=v;}
 public boolean isRulerEnabled(){return rulerEnabled;} public void setRulerEnabled(boolean v){rulerEnabled=v;}
 public int getGridSpacing(){return gridSpacing;} public void setGridSpacing(int v){gridSpacing=Math.max(1,Math.min(64,v));}
 public double snap(double coordinate){return Math.round(coordinate/gridSpacing)*gridSpacing;}
}
