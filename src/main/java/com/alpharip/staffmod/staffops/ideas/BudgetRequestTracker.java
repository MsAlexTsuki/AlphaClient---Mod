package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Tracks resource requests and their review state. */
public final class BudgetRequestTracker {
 public static final String NAME="Budget Request Tracker",CATEGORY="Management",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 public static final class Request {public final String id,requester,description,currency;public final double amount;private String status="PENDING";private Request(String i,String r,String d,double a,String c){id=i;requester=r;description=d;amount=a;currency=c;}public String getStatus(){return status;}}
 private final Map<String,Request> requests=new LinkedHashMap<String,Request>();
 public synchronized void add(String id,String requester,String description,double amount,String currency){if(amount<0||Double.isNaN(amount)||Double.isInfinite(amount))throw new IllegalArgumentException("invalid amount");String i=req(id);requests.put(i,new Request(i,req(requester),req(description),amount,req(currency)));}
 public synchronized void review(String id,String status){Request r=requests.get(req(id));if(r==null)throw new IllegalArgumentException("unknown request");r.status=req(status).toUpperCase(Locale.ROOT);}
 public synchronized List<Request> snapshot(){return Collections.unmodifiableList(new ArrayList<Request>(requests.values()));}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}