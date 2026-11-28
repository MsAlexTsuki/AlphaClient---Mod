package com.alpharip.staffmod.staffops.ideas;

/** Local rejection-state helper; it does not enforce review permissions. */
public final class EvidenceRejectionWorkflow {
 private boolean rejected;
 private String reviewerId="",reason="";
 private long decidedAtMillis;
 public void reject(String reviewerId,String reason,long decidedAtMillis){if(reason==null||reason.trim().isEmpty())throw new IllegalArgumentException("Rejection reason must not be blank");this.rejected=true;this.reviewerId=reviewerId==null?"":reviewerId;this.reason=reason.trim();this.decidedAtMillis=decidedAtMillis;}
 public boolean isRejected(){return rejected;} public String getReviewerId(){return reviewerId;} public String getReason(){return reason;} public long getDecidedAtMillis(){return decidedAtMillis;}
 // TODO(Supabase): rejection must be validated and audited by a trusted service.
}