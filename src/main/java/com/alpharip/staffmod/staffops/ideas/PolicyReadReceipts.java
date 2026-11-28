package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Records acknowledgement timestamps per policy and staff identifier. */
public final class PolicyReadReceipts {
 public static final String NAME="Policy Read Receipts",CATEGORY="Management",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private final Map<String,Map<String,Long>> receipts=new LinkedHashMap<String,Map<String,Long>>();
 public synchronized void acknowledge(String policy,String staff,long time){if(time<0)throw new IllegalArgumentException("time must be non-negative");receipts.computeIfAbsent(req(policy),k->new LinkedHashMap<String,Long>()).put(req(staff),time);}
 public synchronized boolean hasRead(String policy,String staff){Map<String,Long> m=receipts.get(req(policy));return m!=null&&m.containsKey(req(staff));}
 public synchronized Set<String> readers(String policy){Map<String,Long>m=receipts.get(req(policy));return Collections.unmodifiableSet(m==null?new LinkedHashSet<String>():new LinkedHashSet<String>(m.keySet()));}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}