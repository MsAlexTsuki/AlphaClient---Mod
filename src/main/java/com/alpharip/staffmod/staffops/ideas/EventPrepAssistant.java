package com.alpharip.staffmod.staffops.ideas;

import java.util.*;

/** Tracks completion of event preparation checklist items. */
public final class EventPrepAssistant {
 public static final String FEATURE_NAME="Event Prep Assistant",CATEGORY="Cross-Category",STATUS="NEW";
 private final Map<String,Boolean> checklist=new LinkedHashMap<String,Boolean>();
 public void addItem(String item){checklist.put(required(item),Boolean.FALSE);}
 public boolean markDone(String item){String k=required(item);if(!checklist.containsKey(k))return false;checklist.put(k,Boolean.TRUE);return true;}
 public Map<String,Boolean> getChecklist(){return Collections.unmodifiableMap(new LinkedHashMap<String,Boolean>(checklist));}
 public boolean isReady(){return !checklist.isEmpty()&&!checklist.containsValue(Boolean.FALSE);}
 private String required(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("item required");return s.trim();}
}