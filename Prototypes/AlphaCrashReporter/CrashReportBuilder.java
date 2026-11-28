package com.alpharip.staffmod.AlphaCrashReporter;
import java.io.PrintWriter;import java.io.StringWriter;import java.util.UUID;
/** Builds structured records from an exception and explicitly supplied runtime metadata. */
public final class CrashReportBuilder {
 private String client="unknown",minecraft="1.8.9",forge="unknown";private final CrashPrivacyFilter privacy;
 public CrashReportBuilder(CrashPrivacyFilter p){privacy=p==null?new CrashPrivacyFilter():p;}
 public CrashReportBuilder versions(String c,String m,String f){client=clean(c);minecraft=clean(m);forge=clean(f);return this;}
 public CrashReport build(Throwable t,CrashSource source,String module,CrashEnvironment env,boolean includeStack,boolean redact){
  if(t==null)throw new IllegalArgumentException("Throwable is required.");
  StringWriter w=new StringWriter();t.printStackTrace(new PrintWriter(w));String stack=includeStack?w.toString():"";
  String msg=t.getMessage()==null?"":t.getMessage();if(redact){msg=privacy.redact(msg);stack=privacy.redact(stack);}
  CrashCategory cat=CrashClassifier.classify(t,source,stack);CrashSeverity severity=cat==CrashCategory.MEMORY?CrashSeverity.CRITICAL:CrashSeverity.ERROR;
  String type=t.getClass().getName(),fp=CrashFingerprint.create(type,msg,stack);
  String title=type.substring(type.lastIndexOf('.')+1)+(msg.isEmpty()?"":": "+msg);
  return new CrashReport(UUID.randomUUID().toString(),System.currentTimeMillis(),title,type,msg,stack,source,cat,severity,module,fp,client,minecraft,forge,env,CrashReportStatus.NEW);
 }
 private static String clean(String s){return s==null||s.trim().isEmpty()?"unknown":s.trim();}
}
