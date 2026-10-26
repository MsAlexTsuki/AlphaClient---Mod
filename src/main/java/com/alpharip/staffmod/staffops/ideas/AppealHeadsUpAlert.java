package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Associates appeals with case IDs and exposes unseen appeal alerts. */
public final class AppealHeadsUpAlert {
 public static final String NAME="Appeal Heads-Up Alert",CATEGORY="StaffMods",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 public static final class Appeal {public final String id,caseId,summary;public final long createdAt;private boolean seen;private Appeal(String i,String c,String s){id=i;caseId=c;summary=s;createdAt=System.currentTimeMillis();}}
 private final Map<String,Appeal> appeals=new LinkedHashMap<String,Appeal>();
 public synchronized void add(String id,String caseId,String summary){String i=req(id);appeals.put(i,new Appeal(i,req(caseId),req(summary)));}
 public synchronized List<Appeal> unseen(){List<Appeal> out=new ArrayList<Appeal>();for(Appeal a:appeals.values())if(!a.seen)out.add(a);return Collections.unmodifiableList(out);}
 public synchronized void markSeen(String id){Appeal a=appeals.get(req(id));if(a!=null)a.seen=true;}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}