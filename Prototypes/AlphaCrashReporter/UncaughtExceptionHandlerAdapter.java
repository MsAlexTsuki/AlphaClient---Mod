package com.alpharip.staffmod.AlphaCrashReporter;
/** Optional JVM bridge. Install only from an explicit future integration point. */
public final class UncaughtExceptionHandlerAdapter implements Thread.UncaughtExceptionHandler {
 private final CrashDetector detector;private final Thread.UncaughtExceptionHandler previous;private final CrashSource source;
 public UncaughtExceptionHandlerAdapter(CrashDetector d,Thread.UncaughtExceptionHandler p,CrashSource s){detector=d;previous=p;source=s==null?CrashSource.JAVA_UNCAUGHT_HANDLER:s;}
 public void uncaughtException(Thread thread,Throwable error){
  try{detector.capture(error,source,thread==null?"":thread.getName(),CrashEnvironment.capture());}catch(Throwable ignored){}
  if(previous!=null)previous.uncaughtException(thread,error);else if(error!=null)error.printStackTrace();
 }
}
