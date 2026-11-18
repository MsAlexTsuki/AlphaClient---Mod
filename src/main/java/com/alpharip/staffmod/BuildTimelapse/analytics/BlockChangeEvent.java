package com.alpharip.staffmod.BuildTimelapse.analytics;
public final class BlockChangeEvent {
 public final int x,y,z;public final String before,after;public final long time;
 public BlockChangeEvent(int x,int y,int z,String b,String a,long t){if(t<0)throw new IllegalArgumentException("time");this.x=x;this.y=y;this.z=z;before=normalize(b);after=normalize(a);time=t;}
 private static String normalize(String v){return v==null||v.trim().isEmpty()?"air":v.trim();}
 public boolean isPlacement(){return "air".equalsIgnoreCase(before)&&!"air".equalsIgnoreCase(after);}public boolean isRemoval(){return !"air".equalsIgnoreCase(before)&&"air".equalsIgnoreCase(after);}public boolean isChange(){return !before.equalsIgnoreCase(after);}
}