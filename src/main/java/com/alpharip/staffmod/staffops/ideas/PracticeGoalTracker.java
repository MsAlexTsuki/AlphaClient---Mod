package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Tracks weekly goals by ISO week key supplied by the caller. */
public final class PracticeGoalTracker {
 public static final String NAME="Practice Goal Tracker",CATEGORY="Practice",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 public static final class Goal {public final String id,title,weekKey;public final int target;private int progress;private Goal(String i,String t,String w,int n){id=i;title=t;weekKey=w;target=n;}public int getProgress(){return progress;}public int getRemaining(){return Math.max(0,target-progress);}public int getPercent(){return target==0?100:Math.min(100,progress*100/target);}}
 private final Map<String,Goal> goals=new LinkedHashMap<String,Goal>();
 public synchronized Goal add(String id,String title,String weekKey,int target){if(target<1)throw new IllegalArgumentException("target must be positive");Goal g=new Goal(text(id),text(title),text(weekKey),target);goals.put(g.id,g);return g;}
 public synchronized Goal increment(String id,int amount){Goal g=goals.get(text(id));if(g==null)throw new IllegalArgumentException("Unknown goal");if(amount<0)throw new IllegalArgumentException("amount must be non-negative");g.progress=Math.min(g.target,g.progress+amount);return g;}
 public synchronized List<Goal> snapshot(){return Collections.unmodifiableList(new ArrayList<Goal>(goals.values()));}
 private static String text(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}