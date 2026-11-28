package com.alpharip.staffmod.staffops.ideas;

/** Foundation for choosing hold or toggle behavior for individual controls. */
public class HoldOrToggleControlsPerKey {
 private final java.util.Map<String,Boolean> toggleModes=new java.util.LinkedHashMap<String,Boolean>();
 public void setToggleMode(String control,boolean toggle){if(control!=null&&!control.trim().isEmpty())toggleModes.put(control,toggle);}
 public boolean isToggleMode(String control){Boolean value=toggleModes.get(control);return value!=null&&value;}
 public java.util.Map<String,Boolean> getModes(){return java.util.Collections.unmodifiableMap(toggleModes);}
}
