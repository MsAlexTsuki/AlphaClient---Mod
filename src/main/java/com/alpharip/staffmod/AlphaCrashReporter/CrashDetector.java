package com.alpharip.staffmod.AlphaCrashReporter;
import java.io.IOException;
/** Explicit capture service; never hooks Minecraft or JVM automatically. */
public final class CrashDetector {
 private final CrashReportManager manager;private final CrashReporterConfig config;private final CrashReportBuilder builder=new CrashReportBuilder(new CrashPrivacyFilter());
 public CrashDetector(CrashReportManager m,CrashReporterConfig c){manager=m;config=c;}
 public CrashReport capture(Throwable t,CrashSource source,String module,CrashEnvironment env){
  if(!config.isEnabled()||t==null)return null;
  CrashReport r=builder.build(t,source,module,env,config.isCaptureStackTraces(),config.isRedactSensitiveData());
  try{manager.add(r);}catch(IOException e){throw new CrashCaptureException("Could not save crash report.",e);}return r;
 }
}
