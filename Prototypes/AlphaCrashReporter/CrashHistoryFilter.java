package com.alpharip.staffmod.AlphaCrashReporter;
import java.util.Locale;
/** Optional in-memory filter for history screens and future search controls. */
public final class CrashHistoryFilter {
 private String query="";private CrashCategory category;private CrashReportStatus status;
 public CrashHistoryFilter setQuery(String s){query=s==null?"":s.trim();return this;}
 public CrashHistoryFilter setCategory(CrashCategory c){category=c;return this;}
 public CrashHistoryFilter setStatus(CrashReportStatus s){status=s;return this;}
 public boolean matches(CrashReport r){
  if(category!=null&&category!=r.getCategory())return false;if(status!=null&&status!=r.getStatus())return false;
  if(query.isEmpty())return true;String q=query.toLowerCase(Locale.ROOT);
  return has(r.getTitle(),q)||has(r.getExceptionType(),q)||has(r.getMessage(),q)||has(r.getModuleName(),q)||has(r.getFingerprint(),q);
 }
 private static boolean has(String s,String q){return s!=null&&s.toLowerCase(Locale.ROOT).contains(q);}
}
