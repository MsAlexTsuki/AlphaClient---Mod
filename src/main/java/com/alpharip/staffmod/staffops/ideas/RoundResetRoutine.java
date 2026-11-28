package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Configurable between-round checklist with completion state. */
public final class RoundResetRoutine {
 public static final String NAME="Round Reset Routine",CATEGORY="Practice",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private final Map<String,Boolean> items=new LinkedHashMap<String,Boolean>();
 public RoundResetRoutine(){items.put("Food",false);items.put("Potions",false);items.put("Position",false);}
 public synchronized void addItem(String item){items.put(text(item),false);}public synchronized void setDone(String item,boolean done){String k=text(item);if(!items.containsKey(k))throw new IllegalArgumentException("Unknown checklist item");items.put(k,done);}
 public synchronized void reset(){for(String k:new ArrayList<String>(items.keySet()))items.put(k,false);}
 public synchronized boolean isComplete(){return !items.isEmpty()&&!items.containsValue(Boolean.FALSE);}
 public synchronized Map<String,Boolean> snapshot(){return Collections.unmodifiableMap(new LinkedHashMap<String,Boolean>(items));}
 private static String text(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("item required");return s.trim();}
}