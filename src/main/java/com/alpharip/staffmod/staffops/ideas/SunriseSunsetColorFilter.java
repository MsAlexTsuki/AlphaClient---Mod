package com.alpharip.staffmod.staffops.ideas;

/** Foundation for optional sunrise and sunset screen color filters. */
public class SunriseSunsetColorFilter {
 private boolean enabled; private int sunriseColor=0x44FFAA66,sunsetColor=0x445566FF; private float strength=0.25f;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public int getSunriseColor(){return sunriseColor;} public void setSunriseColor(int v){sunriseColor=v;}
 public int getSunsetColor(){return sunsetColor;} public void setSunsetColor(int v){sunsetColor=v;}
 public float getStrength(){return strength;} public void setStrength(float v){strength=Math.max(0f,Math.min(1f,v));}
}
