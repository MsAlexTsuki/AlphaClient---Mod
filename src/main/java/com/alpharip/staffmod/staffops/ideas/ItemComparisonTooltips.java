package com.alpharip.staffmod.staffops.ideas;

/** Foundation for comparing two item-stat snapshots in tooltips. */
public class ItemComparisonTooltips {
 private final java.util.Map<String,Double> firstItem=new java.util.LinkedHashMap<String,Double>(); private final java.util.Map<String,Double> secondItem=new java.util.LinkedHashMap<String,Double>();
 public void setFirstStat(String stat,double value){if(stat!=null)firstItem.put(stat,value);} public void setSecondStat(String stat,double value){if(stat!=null)secondItem.put(stat,value);} public Double getDifference(String stat){Double a=firstItem.get(stat),b=secondItem.get(stat);return a==null||b==null?null:b-a;} public java.util.Map<String,Double> getFirstItem(){return java.util.Collections.unmodifiableMap(firstItem);} public java.util.Map<String,Double> getSecondItem(){return java.util.Collections.unmodifiableMap(secondItem);}
}
