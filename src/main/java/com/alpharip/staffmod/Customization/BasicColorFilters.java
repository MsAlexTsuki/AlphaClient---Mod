package com.alpharip.staffmod.staffops.ideas;

/** Foundation for basic display color-filter preferences. */
public class BasicColorFilters {
 public enum Filter { NONE, GRAYSCALE, HIGH_CONTRAST, WARM, COOL }
 private Filter filter=Filter.NONE; private float intensity=1.0f;
 public Filter getFilter(){return filter;} public void setFilter(Filter v){if(v!=null)filter=v;}
 public float getIntensity(){return intensity;} public void setIntensity(float v){intensity=Math.max(0.0f,Math.min(1.0f,v));}
}
