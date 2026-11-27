package com.alpharip.staffmod.BuildTimelapse.analytics;
public final class BuildArea {
 public final int minX,minY,minZ,maxX,maxY,maxZ;
 public BuildArea(int x1,int y1,int z1,int x2,int y2,int z2){minX=Math.min(x1,x2);minY=Math.min(y1,y2);minZ=Math.min(z1,z2);maxX=Math.max(x1,x2);maxY=Math.max(y1,y2);maxZ=Math.max(z1,z2);}
 public long volume(){long x=(long)maxX-(long)minX+1L,y=(long)maxY-(long)minY+1L,z=(long)maxZ-(long)minZ+1L;try{return Math.multiplyExact(Math.multiplyExact(x,y),z);}catch(ArithmeticException overflow){return Long.MAX_VALUE;}}
 public boolean contains(int x,int y,int z){return x>=minX&&x<=maxX&&y>=minY&&y<=maxY&&z>=minZ&&z<=maxZ;}
}