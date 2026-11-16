package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Queue of rejected cases with a review outcome. */
public final class RejectedCaseReview {
 public static final String NAME="Rejected Case Review",CATEGORY="SeniorStaff",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 public static final class Case {public final String id,reason;private String reviewerNote="";private Case(String i,String r){id=i;reason=r;}public String getReviewerNote(){return reviewerNote;}}
 private final Map<String,Case> cases=new LinkedHashMap<String,Case>();
 public synchronized void add(String id,String reason){String i=req(id);cases.put(i,new Case(i,req(reason)));}
 public synchronized void review(String id,String note){Case c=cases.get(req(id));if(c==null)throw new IllegalArgumentException("unknown case");c.reviewerNote=note==null?"":note.trim();}
 public synchronized List<Case> snapshot(){return Collections.unmodifiableList(new ArrayList<Case>(cases.values()));}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}