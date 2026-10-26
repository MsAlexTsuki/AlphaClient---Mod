package com.alpharip.staffmod.staffops.ideas;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/** Incoming report queue foundation. Server/plugin API support is required to load real reports. */
public final class ReportIntakeTriage {
 public static final String NAME="Report Intake Triage", CATEGORY="Staff", STATUS="NEW";
 public static final boolean REQUIRES_SUPABASE=false, REQUIRES_SERVER_SUPPORT=true;
 // TODO(Server): the server/plugin must supply reports and enforce report access.
 public static final class Report {
  public final String id, summary; public final int priority;
  public Report(String id,String summary,int priority){if(blank(id)||blank(summary))throw new IllegalArgumentException("id and summary required");this.id=id.trim();this.summary=summary.trim();this.priority=Math.max(0,Math.min(5,priority));}
 }
 private final List<Report> queue=new ArrayList<Report>();
 public synchronized void add(Report report){if(report==null)throw new IllegalArgumentException("report");queue.add(report);}
 public synchronized List<Report> getQueue(){List<Report> r=new ArrayList<Report>(queue);Collections.sort(r,(a,b)->Integer.compare(b.priority,a.priority));return Collections.unmodifiableList(r);}
 public synchronized boolean remove(String id){return queue.removeIf(r->r.id.equals(id));}
 private static boolean blank(String s){return s==null||s.trim().isEmpty();}
}