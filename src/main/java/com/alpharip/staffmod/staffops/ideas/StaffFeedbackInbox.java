package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Local feedback inbox with optional anonymous submitter identity. External delivery and persistent storage are separate integrations. */
public final class StaffFeedbackInbox {
 public static final String NAME="Staff Feedback Inbox",CATEGORY="OwnerStaffing",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 public static final class Feedback {public final String id,message,submitter;public final long createdAt;private boolean reviewed;private Feedback(String i,String m,String s,long t){id=i;message=m;submitter=s;createdAt=t;}public boolean isReviewed(){return reviewed;}}
 private final Map<String,Feedback> entries=new LinkedHashMap<String,Feedback>();
 public synchronized Feedback submit(String id,String message,String submitter,boolean anonymous,long time){if(time<0)throw new IllegalArgumentException("time must be non-negative");String i=req(id);Feedback f=new Feedback(i,req(message),anonymous?"Anonymous":(submitter==null?"":submitter.trim()),time);entries.put(i,f);return f;}
 public synchronized void markReviewed(String id){Feedback f=entries.get(req(id));if(f==null)throw new IllegalArgumentException("unknown feedback");f.reviewed=true;}
 public synchronized List<Feedback> snapshot(){return Collections.unmodifiableList(new ArrayList<Feedback>(entries.values()));}
 public synchronized List<Feedback> pending(){List<Feedback> out=new ArrayList<Feedback>();for(Feedback f:entries.values())if(!f.reviewed)out.add(f);return Collections.unmodifiableList(out);}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}