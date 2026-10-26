package com.alpharip.staffmod.staffops.ideas;

/** Foundation for detecting duplicate keybind assignments. */
public class DuplicateKeybindWarning {
 private final java.util.Map<String,Integer> keybinds=new java.util.LinkedHashMap<String,Integer>();
 public void assign(String action,int keyCode){if(action!=null)keybinds.put(action,keyCode);}
 public java.util.List<String> getActionsSharingKey(int keyCode){java.util.List<String> found=new java.util.ArrayList<String>();for(java.util.Map.Entry<String,Integer> e:keybinds.entrySet())if(e.getValue()!=null&&e.getValue()==keyCode)found.add(e.getKey());return found;}
 public java.util.Map<String,Integer> getKeybinds(){return java.util.Collections.unmodifiableMap(keybinds);}
}
