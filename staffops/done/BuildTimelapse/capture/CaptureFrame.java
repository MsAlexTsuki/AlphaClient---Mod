package com.alpharip.staffmod.BuildTimelapse.capture;
public final class CaptureFrame {
 private final long index,time,bytes;private final String path;private final int width,height;
 public CaptureFrame(long i,long t,String p,int w,int h,long b){if(i<0||t<0||p==null||p.trim().isEmpty()||w<1||h<1)throw new IllegalArgumentException("Invalid captured frame");index=i;time=t;path=p;width=w;height=h;bytes=Math.max(0,b);}
 public long getIndex(){return index;}public long getTime(){return time;}public String getPath(){return path;}public int getWidth(){return width;}public int getHeight(){return height;}public long getBytes(){return bytes;}
}