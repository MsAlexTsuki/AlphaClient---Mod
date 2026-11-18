package com.alpharip.staffmod.BuildTimelapse.export;
public final class ExportSettings {
 public enum Format{MP4,GIF,IMAGE_SEQUENCE}private Format format=Format.MP4;private int fps=30,width=1920,height=1080;private String output="timelapse.mp4";
 public synchronized Format getFormat(){return format;}public synchronized void setFormat(Format f){if(f==null)throw new IllegalArgumentException();format=f;}public synchronized int getFps(){return fps;}public synchronized void setFps(int f){if(f<1||f>240)throw new IllegalArgumentException();fps=f;}
 public synchronized int getWidth(){return width;}public synchronized int getHeight(){return height;}public synchronized void setResolution(int w,int h){if(w<16||h<16||w>16384||h>16384)throw new IllegalArgumentException();width=w;height=h;}
 public synchronized String getOutput(){return output;}public synchronized void setOutput(String o){if(o==null||o.trim().isEmpty())throw new IllegalArgumentException();output=o.trim();}
 public synchronized ExportSettings copy(){ExportSettings c=new ExportSettings();c.format=format;c.fps=fps;c.width=width;c.height=height;c.output=output;return c;}
}