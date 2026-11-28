package com.alpharip.staffmod.staffops.ideas;

/** Foundation for an aggressive low-resource visual preset. */
public class MaximumPerformancePotatoMode {
 private boolean enabled; private boolean animations=false, particles=false, fancyGraphics=false; private int renderDistance=2;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public boolean isAnimations(){return animations;} public boolean isParticles(){return particles;} public boolean isFancyGraphics(){return fancyGraphics;}
 public int getRenderDistance(){return renderDistance;}
}
