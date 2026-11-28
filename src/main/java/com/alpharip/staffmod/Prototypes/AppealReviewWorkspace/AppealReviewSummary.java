package com.alpharip.staffmod.AppealReviewWorkspace;
public final class AppealReviewSummary {
 private final int total,submitted,inReview,escalated,decided;
 public AppealReviewSummary(int total,int submitted,int inReview,int escalated,int decided){this.total=total;this.submitted=submitted;this.inReview=inReview;this.escalated=escalated;this.decided=decided;}
 public int getTotal(){return total;}public int getSubmitted(){return submitted;}public int getInReview(){return inReview;}public int getEscalated(){return escalated;}public int getDecided(){return decided;}
}