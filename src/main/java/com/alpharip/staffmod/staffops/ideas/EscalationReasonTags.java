package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Configurable reason tags associated with escalation IDs. */
public final class EscalationReasonTags {
 public static final String NAME="Escalation Reason Tags",CATEGORY="SeniorStaff",STATUS="ENH",REQUIRED_ACCESS="Unconfirmed";
 private final Map<String,Set<String>> tags=new LinkedHashMap<String,Set<String>>();
 public synchronized void add(String id,String tag){tags.computeIfAbsent(req(id),k->new LinkedHashSet<String>()).add(req(tag));}
 public synchronized void remove(String id,String tag){Set<String>s=tags.get(req(id));if(s!=null){s.remove(req(tag));if(s.isEmpty())tags.remove(req(id));}}
 public synchronized Set<String> get(String id){Set<String>s=tags.get(req(id));return Collections.unmodifiableSet(s==null?new LinkedHashSet<String>():new LinkedHashSet<String>(s));}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}