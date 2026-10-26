package com.alpharip.staffmod.staffops.ideas;
import java.util.*;
/** Finds likely duplicate reports using normalized player and summary keys. */
public final class ReportDuplicateFinder {
 public static final String NAME="Report Duplicate Finder",CATEGORY="StaffMods",STATUS="NEW",REQUIRED_ACCESS="Unconfirmed";
 public static final class Report {public final String id,player,summary;private Report(String i,String p,String s){id=i;player=p;summary=s;}}
 private final Map<String,Report> reports=new LinkedHashMap<String,Report>();
 public synchronized void add(String id,String player,String summary){String i=req(id);reports.put(i,new Report(i,req(player),req(summary)));}
 public synchronized List<Report> findDuplicates(String player,String summary){String p=norm(player),s=norm(summary);List<Report> out=new ArrayList<Report>();for(Report r:reports.values())if(norm(r.player).equals(p)||norm(r.summary).equals(s))out.add(r);return Collections.unmodifiableList(out);}
 private static String norm(String s){return req(s).toLowerCase(Locale.ROOT).replaceAll("\\s+"," ");}
 private static String req(String s){if(s==null||s.trim().isEmpty())throw new IllegalArgumentException("value required");return s.trim();}
}