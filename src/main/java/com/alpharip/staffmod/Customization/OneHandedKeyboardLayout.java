package com.alpharip.staffmod.staffops.ideas;

/** Foundation for storing a one-handed keyboard layout mapping. */
public class OneHandedKeyboardLayout {
 private boolean enabled; private final java.util.Map<Integer,Integer> remappings=new java.util.LinkedHashMap<Integer,Integer>();
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public void mapKey(int sourceKey,int targetKey){remappings.put(sourceKey,targetKey);}
 public Integer getMappedKey(int sourceKey){return remappings.get(sourceKey);}
 public void clear(){remappings.clear();}
 public java.util.Map<Integer,Integer> getMappings(){return java.util.Collections.unmodifiableMap(remappings);}
}
