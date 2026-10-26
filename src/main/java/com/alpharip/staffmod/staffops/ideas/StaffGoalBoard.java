package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Tracks goals by team with target date and status. */
public final class StaffGoalBoard {
 public static final String NAME="Staff Goal Board",CATEGORY="Management",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 public static final class Goal {public final String id,team,title;public final long dueAt;private String status="OPEN";private Goal(String i,String t,String n,long d){id=i;team=t;title=n;dueAt=d;}public String getStatus(){return status;}}
 private final Map<String,Goal> goals=new LinkedHashMap<String,Goal>();
 public synchronized void add(String id,String team,String title,long due){if(due<0)throw new IllegalArgumentException("due date must be non-negative");String i=req(id);goals.put(i,new Goal(i,req(team),req(title),due));}
 public synchronized void setStatus(String id,String status){Goal g=goals.get(req(id));if(g==null)throw new IllegalArgumentException("unknown goal");g.status=req(status).toUpperCase(Locale.ROOT);}
 public synchronized List<Goal> snapshot(){return Collections.unmodifiableList(new ArrayList<Goal>(goals.values()));}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}