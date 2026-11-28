package com.alpharip.staffmod.AppealReviewWorkspace;
import java.util.UUID;
public final class AppealDecisionRecord {
 private final String id,appealId,reviewer,rationale;private final AppealDecisionType decision;private final long decidedAt;
 public AppealDecisionRecord(String appeal,AppealDecisionType decision,String reviewer,String rationale,long at){if(appeal==null||appeal.trim().isEmpty()||decision==null||at<=0)throw new IllegalArgumentException("Appeal, decision and timestamp required");id=UUID.randomUUID().toString();appealId=appeal.trim();this.decision=decision;this.reviewer=reviewer==null?"":reviewer.trim();this.rationale=rationale==null?"":rationale.trim();decidedAt=at;}
 public String getId(){return id;}public String getAppealId(){return appealId;}public AppealDecisionType getDecision(){return decision;}public String getReviewer(){return reviewer;}public String getRationale(){return rationale;}public long getDecidedAt(){return decidedAt;}
}