package com.alpharip.staffmod.AlphaCrashReporter;
import java.io.File;
/** Factory for a future explicit integration point; not invoked automatically. */
public final class CrashReporterBootstrap {
 private CrashReporterBootstrap(){}
 public static AlphaCrashReporter create(File gameDirectory){
  if(gameDirectory==null)throw new IllegalArgumentException("Game directory required.");
  return AlphaCrashReporter.open(new File(gameDirectory,"AlphaCrashReporter/reports"),new CrashReporterConfig());
 }
}
