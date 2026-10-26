package com.alpharip.staffmod.staffops.ideas;
/** Computes a configurable warm overlay tint; rendering must be wired into the client HUD. */
public final class GoldenHourOverlay {
 public static final String NAME="Golden Hour Overlay",CATEGORY="OwnerFun",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private boolean enabled=true; private float intensity=0.35f;
 public synchronized void setEnabled(boolean v){enabled=v;} public synchronized boolean isEnabled(){return enabled;}
 public synchronized void setIntensity(float v){if(Float.isNaN(v)||v<0f||v>1f)throw new IllegalArgumentException("intensity must be 0..1");intensity=v;}
 public synchronized float getIntensity(){return enabled?intensity:0f;}
 public synchronized int blend(int original,int warm){float a=getIntensity();int out=0;for(int shift:new int[]{16,8,0}){int x=(original>>shift)&255,y=(warm>>shift)&255;out|=Math.round(x+(y-x)*a)<<shift;}return (original&0xff000000)|(out&0xffffff);}
}