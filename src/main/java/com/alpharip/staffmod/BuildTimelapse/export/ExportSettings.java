package com.alpharip.staffmod.BuildTimelapse.export;
public final class ExportSettings {
 public enum Format{MP4,GIF,IMAGE_SEQUENCE}private Format format=Format.MP4;private int fps=30,width=1920,height=1080;private String output="timelapse.mp4";
 public Format getFormat(){return format;}public void setFormat(Format f){if(f==null)throw new IllegalArgumentException();format=f;}public int getFps(){return fps;}public void setFps(int f){if(f<1||f>240)throw new IllegalArgumentException();fps=f;}
 public int getWidth(){return width;}public int getHeight(){return height;}public void setResolution(int w,int h){if(w<16||h<16||w>16384||h>16384)throw new IllegalArgumentException();width=w;height=h;}
 public String getOutput(){return output;}public void setOutput(String o){if(o==null||o.trim().isEmpty())throw new IllegalArgumentException();output=o.trim();}
}