package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Checklist of raid supplies and required quantities. */
public final class RaidSupplyChecklist {
 public static final String NAME="Raid Supply Checklist",CATEGORY="HardcoreFactions",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 public static final class Supply {public final String item;public final int required;private int packed;private Supply(String i,int r){item=i;required=r;}public int getPacked(){return packed;}public boolean isReady(){return packed>=required;}}
 private final Map<String,Supply> supplies=new LinkedHashMap<String,Supply>();
 public synchronized void add(String item,int required){if(required<0)throw new IllegalArgumentException("required must be non-negative");supplies.put(text(item),new Supply(text(item),required));}
 public synchronized void setPacked(String item,int packed){if(packed<0)throw new IllegalArgumentException("packed must be non-negative");Supply s=supplies.get(text(item));if(s==null)throw new IllegalArgumentException("Unknown supply");s.packed=packed;}
 public synchronized boolean isReady(){for(Supply s:supplies.values())if(!s.isReady())return false;return true;}
 public synchronized List<Supply> snapshot(){return Collections.unmodifiableList(new ArrayList<Supply>(supplies.values()));}
 private static String text(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("item required");return s.trim();}
}