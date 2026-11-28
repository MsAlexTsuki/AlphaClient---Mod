package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Schedules staff reviews and tracks completion. */
public final class QuarterlyReviewPlanner {
 public static final String NAME="Quarterly Review Planner",CATEGORY="Management",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 public static final class Review {public final String id,staff;public final long scheduledAt;private boolean complete;private Review(String i,String s,long t){id=i;staff=s;scheduledAt=t;}public boolean isComplete(){return complete;}}
 private final Map<String,Review> reviews=new LinkedHashMap<String,Review>();
 public synchronized void schedule(String id,String staff,long time){if(time<0)throw new IllegalArgumentException("time must be non-negative");String i=req(id);reviews.put(i,new Review(i,req(staff),time));}
 public synchronized void complete(String id,boolean done){Review r=reviews.get(req(id));if(r==null)throw new IllegalArgumentException("unknown review");r.complete=done;}
 public synchronized List<Review> snapshot(){return Collections.unmodifiableList(new ArrayList<Review>(reviews.values()));}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}