package com.alpharip.staffmod.staffops.ideas;

/** Foundation for build symmetry guide configuration. */
public class BuildSymmetryGuides {
 private boolean enabled; private int axis; private int originX; private int originZ;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;} public int getAxis(){return axis;} public void setAxis(int v){axis=Math.max(0,Math.min(2,v));} public void setOrigin(int x,int z){originX=x;originZ=z;} public int getOriginX(){return originX;} public int getOriginZ(){return originZ;}
}
