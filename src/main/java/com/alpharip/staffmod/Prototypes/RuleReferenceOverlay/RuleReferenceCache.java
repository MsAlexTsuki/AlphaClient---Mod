package com.alpharip.staffmod.RuleReferenceOverlay;
import java.util.*;
/** Session-local cache only; no disk or remote synchronization. */
public final class RuleReferenceCache {
 private final Map<String,List<RuleEntry>> byCategory=new EnumMap<String,List<RuleEntry>>(RuleCategory.class);
 public synchronized void replace(RuleCategory category,List<RuleEntry> rules){if(category==null)throw new IllegalArgumentException("category");byCategory.put(category,new ArrayList<RuleEntry>(rules==null?Collections.<RuleEntry>emptyList():rules));}
 public synchronized List<RuleEntry> get(RuleCategory category){List<RuleEntry> v=byCategory.get(category);return v==null?Collections.<RuleEntry>emptyList():Collections.unmodifiableList(new ArrayList<RuleEntry>(v));}
 public synchronized void clear(){byCategory.clear();}
}