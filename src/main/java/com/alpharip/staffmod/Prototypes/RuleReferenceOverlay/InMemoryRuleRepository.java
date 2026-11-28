package com.alpharip.staffmod.RuleReferenceOverlay;
import java.util.*;
public final class InMemoryRuleRepository implements RuleRepository {
 private final Map<String,RuleEntry> rules=new LinkedHashMap<String,RuleEntry>();
 public synchronized void put(RuleEntry rule){if(rule==null)throw new IllegalArgumentException("rule");rules.put(rule.getId(),rule);}
 public synchronized void remove(String id){rules.remove(id);}
 public synchronized List<RuleEntry> getAll(){return Collections.unmodifiableList(new ArrayList<RuleEntry>(rules.values()));}
 public synchronized RuleEntry findById(String id){return rules.get(id);}
}