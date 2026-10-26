package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Inventory-independent key count model. Minecraft inventory scanning must call setCount for each configured key. */
public final class KeyStockCounter {
 public static final String NAME="Key Stock Counter",CATEGORY="KitMap",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private final Map<String,Integer> counts=new LinkedHashMap<String,Integer>(),thresholds=new LinkedHashMap<String,Integer>();
 public synchronized void configure(String key,int threshold){String k=text(key);if(threshold<0)throw new IllegalArgumentException("threshold must not be negative");thresholds.put(k,threshold);}
 public synchronized void setCount(String key,int count){String k=text(key);if(count<0)throw new IllegalArgumentException("count must not be negative");counts.put(k,count);}
 public synchronized int getCount(String key){return counts.containsKey(text(key))?counts.get(key):0;}
 public synchronized boolean isLow(String key){String k=text(key);return getCount(k)<(thresholds.containsKey(k)?thresholds.get(k):0);}
 public synchronized Map<String,Integer> snapshot(){return Collections.unmodifiableMap(new LinkedHashMap<String,Integer>(counts));}
 private static String text(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("key must not be blank");return s.trim();}
}