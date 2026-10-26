package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Shift-start checklist state model. */
public final class ShiftStartChecklist {
 public static final String NAME="Shift Start Checklist",CATEGORY="StaffMods",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private final Map<String,Boolean> items=new LinkedHashMap<String,Boolean>();
 public synchronized void add(String item){items.put(req(item),false);}public synchronized void setDone(String item,boolean done){String k=req(item);if(!items.containsKey(k))throw new IllegalArgumentException("Unknown item");items.put(k,done);}
 public synchronized void reset(){for(String k:new ArrayList<String>(items.keySet()))items.put(k,false);}
 public synchronized boolean isComplete(){return !items.isEmpty()&&!items.containsValue(Boolean.FALSE);}
 public synchronized Map<String,Boolean> snapshot(){return Collections.unmodifiableMap(new LinkedHashMap<String,Boolean>(items));}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("item required");return s.trim();}
}