package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Counts active mentees per mentor. */
public final class MentorLoadMeter {
 public static final String NAME="Mentor Load Meter",CATEGORY="SeniorStaff",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private final Map<String,Set<String>> assignments=new LinkedHashMap<String,Set<String>>();
 public synchronized void assign(String mentor,String trial){assignments.computeIfAbsent(req(mentor),k->new LinkedHashSet<String>()).add(req(trial));}
 public synchronized void unassign(String mentor,String trial){Set<String>s=assignments.get(req(mentor));if(s!=null){s.remove(req(trial));if(s.isEmpty())assignments.remove(req(mentor));}}
 public synchronized int load(String mentor){Set<String>s=assignments.get(req(mentor));return s==null?0:s.size();}
 public synchronized Map<String,Integer> snapshot(){Map<String,Integer> out=new LinkedHashMap<String,Integer>();for(Map.Entry<String,Set<String>>e:assignments.entrySet())out.put(e.getKey(),e.getValue().size());return Collections.unmodifiableMap(out);}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}