package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Configurable low-stock thresholds to complement Base Storage Ledger. Inventory/storage values must be supplied by that feature. */
public final class StorageLowAlert {
 public static final String NAME="Storage Low Alert",CATEGORY="HardcoreFactions",STATUS="ENH",REQUIRED_ACCESS="Unconfirmed";
 private final Map<String,Integer> thresholds=new LinkedHashMap<String,Integer>(),stock=new LinkedHashMap<String,Integer>();
 public synchronized void setThreshold(String item,int minimum){if(minimum<0)throw new IllegalArgumentException("minimum must be non-negative");thresholds.put(req(item),minimum);}
 public synchronized void updateStock(String item,int count){if(count<0)throw new IllegalArgumentException("count must be non-negative");stock.put(req(item),count);}
 public synchronized boolean isLow(String item){String k=req(item);return thresholds.containsKey(k)&&stock.containsKey(k)&&stock.get(k)<thresholds.get(k);}
 public synchronized List<String> lowItems(){List<String> out=new ArrayList<String>();for(String item:thresholds.keySet())if(isLow(item))out.add(item);return Collections.unmodifiableList(out);}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("item required");return s.trim();}
}