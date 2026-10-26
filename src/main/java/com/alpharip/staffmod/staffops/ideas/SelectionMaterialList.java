package com.alpharip.staffmod.staffops.ideas;

/** Foundation for a material list for a selected build area. */
public class SelectionMaterialList {
 private int selectedBlocks; private final java.util.Map<String,Integer> materialCounts=new java.util.LinkedHashMap<String,Integer>();
 public void clear(){selectedBlocks=0;materialCounts.clear();} public void addMaterial(String material,int count){if(material==null||material.trim().isEmpty()||count<=0)return;materialCounts.put(material,materialCounts.containsKey(material)?materialCounts.get(material)+count:count);selectedBlocks+=count;} public int getSelectedBlocks(){return selectedBlocks;} public java.util.Map<String,Integer> getMaterialCounts(){return java.util.Collections.unmodifiableMap(materialCounts);}
}
