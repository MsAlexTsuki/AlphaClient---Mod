package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Tracks meeting action items and completion. */
public final class MeetingActionTracker {
 public static final String NAME="Meeting Action Tracker",CATEGORY="Management",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 public static final class Action {public final String id,meeting,description,owner;public final long dueAt;private boolean done;private Action(String i,String m,String d,String o,long due){id=i;meeting=m;description=d;owner=o;dueAt=due;}public boolean isDone(){return done;}}
 private final Map<String,Action> actions=new LinkedHashMap<String,Action>();
 public synchronized void add(String id,String meeting,String description,String owner,long due){if(due<0)throw new IllegalArgumentException("due date must be non-negative");String i=req(id);actions.put(i,new Action(i,req(meeting),req(description),req(owner),due));}
 public synchronized void setDone(String id,boolean done){Action a=actions.get(req(id));if(a==null)throw new IllegalArgumentException("unknown action");a.done=done;}
 public synchronized List<Action> open(){List<Action> out=new ArrayList<Action>();for(Action a:actions.values())if(!a.done)out.add(a);return Collections.unmodifiableList(out);}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}