package com.alpharip.staffmod.staffops.ideas;

/** Foundation for presets selected by PC performance tier. */
public class PCTierPerformancePresets {
 public enum Tier { LOW, BALANCED, HIGH, CUSTOM }
 private Tier tier=Tier.BALANCED; private int renderDistance=8; private int backgroundFps=30; private boolean particles=true;
 public Tier getTier(){return tier;} public void setTier(Tier v){if(v!=null)tier=v;}
 public int getRenderDistance(){return renderDistance;} public void setRenderDistance(int v){renderDistance=Math.max(2,Math.min(32,v));}
 public int getBackgroundFps(){return backgroundFps;} public void setBackgroundFps(int v){backgroundFps=Math.max(1,Math.min(240,v));}
 public boolean isParticles(){return particles;} public void setParticles(boolean v){particles=v;}
}
