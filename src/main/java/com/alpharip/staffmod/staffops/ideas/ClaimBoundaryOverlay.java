package com.alpharip.staffmod.staffops.ideas;
/** Claim rectangle geometry model; world rendering is not connected. */
public class ClaimBoundaryOverlay {private int minX,minZ,maxX,maxZ;private boolean configured,enabled=true;
 public void setBounds(int x1,int z1,int x2,int z2){minX=Math.min(x1,x2);maxX=Math.max(x1,x2);minZ=Math.min(z1,z2);maxZ=Math.max(z1,z2);configured=true;}
 public boolean contains(int x,int z){return configured&&x>=minX&&x<=maxX&&z>=minZ&&z<=maxZ;}public boolean isConfigured(){return configured;}public boolean isEnabled(){return enabled;}public void setEnabled(boolean v){enabled=v;}
 public int getMinX(){return minX;}public int getMaxX(){return maxX;}public int getMinZ(){return minZ;}public int getMaxZ(){return maxZ;}
}