package com.alpharip.staffmod.AppealReviewWorkspace;
public final class AppealReviewerAssignment {
 private final String appealId,reviewer;private final long assignedAt;
 public AppealReviewerAssignment(String id,String reviewer,long at){if(id==null||id.trim().isEmpty()||reviewer==null||reviewer.trim().isEmpty()||at<=0)throw new IllegalArgumentException("Appeal, reviewer and timestamp required");appealId=id.trim();this.reviewer=reviewer.trim();assignedAt=at;}
 public String getAppealId(){return appealId;}public String getReviewer(){return reviewer;}public long getAssignedAt(){return assignedAt;}
}