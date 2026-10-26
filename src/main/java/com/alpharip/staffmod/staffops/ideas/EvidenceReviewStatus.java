package com.alpharip.staffmod.staffops.ideas;

/** Local review-state model; these values do not authorize evidence actions. */
public final class EvidenceReviewStatus {
 public enum Status { PENDING, IN_REVIEW, APPROVED, REJECTED, NEEDS_CHANGES }
 private Status status=Status.PENDING;
 private String note="";
 public Status getStatus(){return status;}
 public void setStatus(Status status){if(status==null)throw new IllegalArgumentException("Status is required");this.status=status;}
 public String getNote(){return note;}
 public void setNote(String note){this.note=note==null?"":note;}
}