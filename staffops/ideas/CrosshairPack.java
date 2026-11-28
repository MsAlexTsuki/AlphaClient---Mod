package com.alpharip.staffmod.staffops.ideas;

import java.util.*;

/** Provides named crosshair preset definitions for a later renderer. */
public final class CrosshairPack {
 public static final String FEATURE_NAME="Crosshair Pack",CATEGORY="Donator Elite",STATUS="NEW";
 private final Map<String,Integer> sizes=new LinkedHashMap<String,Integer>();
 public CrosshairPack(){sizes.put("dot",2);sizes.put("classic",8);sizes.put("cross",10);}
 public void putPreset(String name,int size){if(name==null||name.trim().isEmpty())throw new IllegalArgumentException("name required");if(size<1||size>32)throw new IllegalArgumentException("size must be 1-32");sizes.put(name.trim(),size);}
 public Integer getSize(String name){return sizes.get(name);}
 public Map<String,Integer> presets(){return Collections.unmodifiableMap(new LinkedHashMap<String,Integer>(sizes));}
}