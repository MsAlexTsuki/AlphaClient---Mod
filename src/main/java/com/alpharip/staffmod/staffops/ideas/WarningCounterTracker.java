package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Counts recorded warnings by normalized player identifier. */
public final class WarningCounterTracker {
 public static final String NAME="Warning Counter Tracker",CATEGORY="StaffMods",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private final Map<String,Integer> counts=new LinkedHashMap<String,Integer>();
 public synchronized int record(String player){String p=req(player).toLowerCase(Locale.ROOT);int n=counts.containsKey(p)?counts.get(p)+1:1;counts.put(p,n);return n;}
 public synchronized int get(String player){String p=req(player).toLowerCase(Locale.ROOT);return counts.containsKey(p)?counts.get(p):0;}
 public synchronized void reset(String player){counts.remove(req(player).toLowerCase(Locale.ROOT));}
 public synchronized Map<String,Integer> snapshot(){return Collections.unmodifiableMap(new LinkedHashMap<String,Integer>(counts));}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("player required");return s.trim();}
}