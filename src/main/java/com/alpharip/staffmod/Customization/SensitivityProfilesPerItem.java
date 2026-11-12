package com.alpharip.staffmod.staffops.ideas;

/** Foundation for sensitivity values associated with item identifiers. */
public class SensitivityProfilesPerItem {
 private final java.util.Map<String,Float> sensitivities=new java.util.LinkedHashMap<String,Float>();
 public void setSensitivity(String item,float value){if(item!=null&&!item.trim().isEmpty())sensitivities.put(item,Math.max(0.01f,Math.min(10.0f,value)));}
 public Float getSensitivity(String item){return sensitivities.get(item);}
 public java.util.Map<String,Float> getProfiles(){return java.util.Collections.unmodifiableMap(sensitivities);}
 public void remove(String item){sensitivities.remove(item);}
}
