package com.alpharip.staffmod.AlphaCrashReporter;
import java.util.*;
public final class CrashAnalysisResult {
 private final CrashCategory category;private final CrashSeverity severity;private final String summary,fingerprint;private final List<SuggestedFix> fixes;
 CrashAnalysisResult(CrashCategory c,CrashSeverity s,String summary,List<SuggestedFix> fixes,String fp){category=c;severity=s;this.summary=summary;this.fixes=Collections.unmodifiableList(new ArrayList<SuggestedFix>(fixes));fingerprint=fp;}
 public CrashCategory getCategory(){return category;}public CrashSeverity getSeverity(){return severity;}
 public String getSummary(){return summary;}public List<SuggestedFix> getSuggestedFixes(){return fixes;}public String getFingerprint(){return fingerprint;}
}
