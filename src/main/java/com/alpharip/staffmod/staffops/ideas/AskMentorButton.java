package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Creates a structured mentor request; delivery to a real mentor requires integration. */
public final class AskMentorButton {
 public static final String NAME="Ask Mentor Button",CATEGORY="TrialModerator",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 public static final class Request {public final String id,author,question;public final long createdAt;private Request(String i,String a,String q){id=i;author=a;question=q;createdAt=System.currentTimeMillis();}}
 private final List<Request> requests=new ArrayList<Request>();
 public synchronized Request create(String id,String author,String question){Request r=new Request(req(id),req(author),req(question));requests.add(r);return r;}
 public synchronized List<Request> snapshot(){return Collections.unmodifiableList(new ArrayList<Request>(requests));}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}