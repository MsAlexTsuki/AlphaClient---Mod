package com.alpharip.staffmod.staffops.ideas;

import java.time.LocalDate;
import java.util.*;

/** Stores daily challenge definitions and completion dates in memory. */
public final class AlphaDailyChallenges {
    public static final String FEATURE_NAME="Alpha Daily Challenges", CATEGORY="Cross-Category", STATUS="NEW";
    public static final class Challenge {public final String id,description;private LocalDate completedOn;private Challenge(String i,String d){id=i;description=d;}}
    private final Map<String,Challenge> challenges=new LinkedHashMap<String,Challenge>();
    public synchronized void add(String id,String description){String i=req(id);challenges.put(i,new Challenge(i,req(description)));}
    public synchronized boolean complete(String id,LocalDate date){Challenge c=challenges.get(id);if(c==null)return false;c.completedOn=date==null?LocalDate.now():date;return true;}
    public synchronized boolean isCompleteToday(String id,LocalDate date){Challenge c=challenges.get(id);return c!=null&&Objects.equals(c.completedOn,date==null?LocalDate.now():date);}
    public synchronized List<Challenge> all(){return Collections.unmodifiableList(new ArrayList<Challenge>(challenges.values()));}
    private String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}