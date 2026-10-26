package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Tracks onboarding steps for a new staff member. */
public final class FirstWeekRoadmap {
 public static final String NAME="First Week Roadmap",CATEGORY="TrialModerator",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 private final Map<String,Boolean> steps=new LinkedHashMap<String,Boolean>();
 public FirstWeekRoadmap(){for(String s:Arrays.asList("Read staff rules","Review commands","Shadow a shift","Complete a practice case","Meet mentor"))steps.put(s,false);}
 public synchronized void addStep(String step){steps.put(req(step),false);}public synchronized void complete(String step,boolean done){String s=req(step);if(!steps.containsKey(s))throw new IllegalArgumentException("unknown step");steps.put(s,done);}
 public synchronized int completionPercent(){if(steps.isEmpty())return 100;int n=0;for(boolean v:steps.values())if(v)n++;return n*100/steps.size();}
 public synchronized Map<String,Boolean> snapshot(){return Collections.unmodifiableMap(new LinkedHashMap<String,Boolean>(steps));}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}