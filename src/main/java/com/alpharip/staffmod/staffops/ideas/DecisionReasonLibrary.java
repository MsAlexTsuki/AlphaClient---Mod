package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Reusable decision reason library. */
public final class DecisionReasonLibrary {
 public static final String NAME="Decision Reason Library",CATEGORY="SeniorStaff",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private final Map<String,String> reasons=new LinkedHashMap<String,String>();
 public synchronized void put(String id,String description){reasons.put(req(id),req(description));}
 public synchronized String get(String id){return reasons.get(req(id));}
 public synchronized boolean remove(String id){return reasons.remove(req(id))!=null;}
 public synchronized Map<String,String> snapshot(){return Collections.unmodifiableMap(new LinkedHashMap<String,String>(reasons));}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}