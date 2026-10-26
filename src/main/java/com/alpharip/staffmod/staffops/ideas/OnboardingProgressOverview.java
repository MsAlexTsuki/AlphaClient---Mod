package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Tracks completion of onboarding steps per staff member. */
public final class OnboardingProgressOverview {
 public static final String NAME="Onboarding Progress Overview",CATEGORY="OwnerStaffing",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private final Map<String,LinkedHashMap<String,Boolean>> people=new LinkedHashMap<String,LinkedHashMap<String,Boolean>>();
 public synchronized void addStaff(String id,Collection<String> steps){String k=req(id);LinkedHashMap<String,Boolean> m=new LinkedHashMap<String,Boolean>();if(steps!=null)for(String s:steps)m.put(req(s),false);people.put(k,m);}
 public synchronized void complete(String id,String step,boolean done){LinkedHashMap<String,Boolean> m=people.get(req(id));if(m==null||!m.containsKey(req(step)))throw new IllegalArgumentException("unknown staff member or step");m.put(step.trim(),done);}
 public synchronized int percent(String id){Map<String,Boolean> m=people.get(req(id));if(m==null)throw new IllegalArgumentException("unknown staff member");if(m.isEmpty())return 100;int n=0;for(Boolean b:m.values())if(Boolean.TRUE.equals(b))n++;return n*100/m.size();}
 public synchronized Map<String,Integer> overview(){Map<String,Integer> out=new LinkedHashMap<String,Integer>();for(String id:people.keySet())out.put(id,percent(id));return Collections.unmodifiableMap(out);}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}