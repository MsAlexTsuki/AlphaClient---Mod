package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Curates named Alpha milestones for a founder-only gallery; UI and access enforcement remain external. */
public final class FoundersWallOfFame {
 public static final String NAME="Founder's Wall of Fame",CATEGORY="FounderFun",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 public static final class Milestone {public final String id,title,detail;public final long date;private Milestone(String i,String t,String d,long dt){id=i;title=t;detail=d;date=dt;}}
 private final Map<String,Milestone> items=new LinkedHashMap<String,Milestone>();
 public synchronized void add(String id,String title,String detail,long date){if(date<0)throw new IllegalArgumentException("date must be non-negative");String k=req(id);items.put(k,new Milestone(k,req(title),detail==null?"":detail.trim(),date));}
 public synchronized List<Milestone> entries(){return Collections.unmodifiableList(new ArrayList<Milestone>(items.values()));}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}