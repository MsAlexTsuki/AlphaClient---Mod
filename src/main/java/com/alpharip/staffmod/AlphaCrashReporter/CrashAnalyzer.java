package com.alpharip.staffmod.AlphaCrashReporter;
public final class CrashAnalyzer {
 private final CrashFixCatalog catalog;
 public CrashAnalyzer(CrashFixCatalog c){catalog=c==null?new CrashFixCatalog():c;}
 public CrashAnalysisResult analyze(CrashReport r){
  if(r==null)throw new IllegalArgumentException("Report required.");
  String summary="No root cause established. Category and suggestions are heuristic.";
  if(r.getCategory()==CrashCategory.MEMORY)summary="The report contains memory-exhaustion indicators.";
  else if(r.getCategory()==CrashCategory.GRAPHICS)summary="The report contains graphics or rendering indicators.";
  else if(r.getCategory()==CrashCategory.FORGE)summary="The report contains Forge or mod-loading indicators.";
  else if(r.getCategory()==CrashCategory.ALPHA_MODULE)summary="The stack trace contains Alpha module package references.";
  return new CrashAnalysisResult(r.getCategory(),r.getSeverity(),summary,catalog.suggest(r),r.getFingerprint());
 }
}
