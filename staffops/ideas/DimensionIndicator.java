package com.alpharip.staffmod.staffops.ideas;

/** Foundation for a compact dimension indicator. */
public class DimensionIndicator {
 private boolean enabled=true; private String dimension="Overworld";
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public String getDimension(){return dimension;}
 public void setDimension(String value){dimension=value==null||value.trim().isEmpty()?"Unknown":value;}
}
