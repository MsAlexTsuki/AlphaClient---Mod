package com.alpharip.staffmod.staffops.ideas;

import java.util.*;

/** Turns supplied kit item names into a simple ordered practice drill outline. */
public final class KitToDrillSync {
 public static final String FEATURE_NAME="Kit-to-Drill Sync",CATEGORY="Cross-Category",STATUS="NEW";
 public List<String> createDrill(String kitName,List<String> items){
  if(kitName==null||kitName.trim().isEmpty())throw new IllegalArgumentException("kit name required");
  List<String> steps=new ArrayList<String>();steps.add("Warm up with kit: "+kitName.trim());
  if(items!=null)for(String item:items)if(item!=null&&!item.trim().isEmpty())steps.add("Practice using "+item.trim());
  steps.add("Finish with a full kit simulation");return Collections.unmodifiableList(steps);
 }
}