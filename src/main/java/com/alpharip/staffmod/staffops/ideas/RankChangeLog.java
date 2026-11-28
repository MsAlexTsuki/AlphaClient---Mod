package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Local rank-change audit entries; does not change ranks or replace the existing audit viewer. */
public final class RankChangeLog {
 public static final String NAME="Rank Change Log",CATEGORY="OwnerStaffing",STATUS="ENH",REQUIRED_ACCESS="Unconfirmed";
 public static final class Change {public final String player,oldRank,newRank,actor,reason;public final long time;private Change(String p,String o,String n,String a,String r,long t){player=p;oldRank=o;newRank=n;actor=a;reason=r;time=t;}}
 private final List<Change> changes=new ArrayList<Change>();
 public synchronized void record(String player,String oldRank,String newRank,String actor,String reason,long time){if(time<0)throw new IllegalArgumentException("time must be non-negative");changes.add(new Change(req(player),oldRank==null?"":oldRank.trim(),req(newRank),req(actor),reason==null?"":reason.trim(),time));}
 public synchronized List<Change> snapshot(){return Collections.unmodifiableList(new ArrayList<Change>(changes));}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}