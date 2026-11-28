package com.alpharip.staffmod.AlphaCrashReporter;
import java.util.Date;
/** Crash report model. Status is mutable; captured diagnostic fields are immutable. */
public final class CrashReport {
 private final String id,title,type,message,stack,module,fingerprint,clientVersion,minecraftVersion,forgeVersion;
 private final long timestamp; private final CrashSource source; private final CrashCategory category;
 private final CrashSeverity severity; private final CrashEnvironment environment; private CrashReportStatus status;
 CrashReport(String id,long time,String title,String type,String message,String stack,CrashSource source,CrashCategory category,CrashSeverity severity,String module,String fingerprint,String client,String mc,String forge,CrashEnvironment env,CrashReportStatus status){
  this.id=id;timestamp=time;this.title=s(title);this.type=s(type);this.message=s(message);this.stack=s(stack);
  this.source=source==null?CrashSource.UNKNOWN:source;this.category=category==null?CrashCategory.UNKNOWN:category;
  this.severity=severity==null?CrashSeverity.ERROR:severity;this.module=s(module);this.fingerprint=s(fingerprint);
  clientVersion=s(client);minecraftVersion=s(mc);forgeVersion=s(forge);environment=env==null?new CrashEnvironment(null):env;
  this.status=status==null?CrashReportStatus.NEW:status;
 }
 private static String s(String v){return v==null?"":v;}
 public String getId(){return id;} public long getTimestamp(){return timestamp;} public Date getDate(){return new Date(timestamp);}
 public String getTitle(){return title;} public String getExceptionType(){return type;} public String getMessage(){return message;}
 public String getStackTrace(){return stack;} public CrashSource getSource(){return source;} public CrashCategory getCategory(){return category;}
 public CrashSeverity getSeverity(){return severity;} public String getModuleName(){return module;} public String getFingerprint(){return fingerprint;}
 public String getClientVersion(){return clientVersion;} public String getMinecraftVersion(){return minecraftVersion;}
 public String getForgeVersion(){return forgeVersion;} public CrashEnvironment getEnvironment(){return environment;}
 public CrashReportStatus getStatus(){return status;} public void setStatus(CrashReportStatus v){if(v!=null)status=v;}
}
