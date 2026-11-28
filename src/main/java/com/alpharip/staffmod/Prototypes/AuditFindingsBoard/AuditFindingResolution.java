package com.alpharip.staffmod.AuditFindingsBoard;
public final class AuditFindingResolution {
 private final String outcome,rationale,resolvedBy;private final long resolvedAt;
 public AuditFindingResolution(String outcome,String rationale,String resolvedBy,long resolvedAt){
  if(outcome==null||outcome.trim().isEmpty()||rationale==null||rationale.trim().isEmpty())throw new IllegalArgumentException("Outcome and rationale are required");
  this.outcome=outcome.trim();this.rationale=rationale.trim();this.resolvedBy=resolvedBy==null?"":resolvedBy.trim();this.resolvedAt=Math.max(0L,resolvedAt);
 }
 public String getOutcome(){return outcome;}public String getRationale(){return rationale;}public String getResolvedBy(){return resolvedBy;}public long getResolvedAt(){return resolvedAt;}
}
