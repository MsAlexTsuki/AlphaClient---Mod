package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Identifies cases whose last activity is older than a caller-selected threshold. */
public final class StaleCaseSweeper {
 public static final String NAME="Stale Case Sweeper",CATEGORY="SeniorStaff",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 public static final class CaseActivity {public final String id;public final long lastActivity;private CaseActivity(String i,long t){id=i;lastActivity=t;}}
 private final Map<String,CaseActivity> cases=new LinkedHashMap<String,CaseActivity>();
 public synchronized void update(String id,long lastActivity){if(lastActivity<0)throw new IllegalArgumentException("timestamp must be non-negative");String i=req(id);cases.put(i,new CaseActivity(i,lastActivity));}
 public synchronized List<CaseActivity> stale(long now,long threshold){if(threshold<0||now<0)throw new IllegalArgumentException("invalid time");List<CaseActivity> out=new ArrayList<CaseActivity>();for(CaseActivity c:cases.values())if(now>=c.lastActivity&&now-c.lastActivity>=threshold)out.add(c);return Collections.unmodifiableList(out);}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("id required");return s.trim();}
}