package com.alpharip.staffmod.staffops.ideas;

/** Foundation for storing community-shared configuration preset metadata. */
public class CommunityPresets {
 public static final class Preset { public final String id,name,author; public Preset(String i,String n,String a){id=i;name=n;author=a;} }
 private final java.util.Map<String,Preset> presets=new java.util.LinkedHashMap<String,Preset>();
 public void add(String id,String name,String author){if(id!=null&&!id.trim().isEmpty())presets.put(id,new Preset(id,name==null?"":name,author==null?"":author));}
 public Preset get(String id){return presets.get(id);} public boolean remove(String id){return presets.remove(id)!=null;}
 public java.util.Map<String,Preset> getAll(){return java.util.Collections.unmodifiableMap(presets);}
}
