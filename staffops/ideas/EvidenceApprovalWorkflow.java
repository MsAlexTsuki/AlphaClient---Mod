package com.alpharip.staffmod.staffops.ideas;

/** Local approval-state helper; it is not an authorization or enforcement mechanism. */
public final class EvidenceApprovalWorkflow {
 private boolean approved;
 private String reviewerId="",comment="";
 private long decidedAtMillis;
 public void approve(String reviewerId,String comment,long decidedAtMillis){this.approved=true;this.reviewerId=reviewerId==null?"":reviewerId;this.comment=comment==null?"":comment;this.decidedAtMillis=decidedAtMillis;}
 public boolean isApproved(){return approved;} public String getReviewerId(){return reviewerId;} public String getComment(){return comment;} public long getDecidedAtMillis(){return decidedAtMillis;}
 // TODO(Supabase): approval must be validated and audited by a trusted service.
}