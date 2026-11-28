package com.alpharip.staffmod.staffops.ideas;

/** Local assignment record; it does not notify or grant access to the assigned reviewer. */
public final class EvidenceReviewerAssignment {
 private String evidenceReference="",reviewerId="",assignedBy="";
 private long assignedAtMillis;
 public void assign(String evidenceReference,String reviewerId,String assignedBy,long assignedAtMillis){this.evidenceReference=evidenceReference==null?"":evidenceReference;this.reviewerId=reviewerId==null?"":reviewerId;this.assignedBy=assignedBy==null?"":assignedBy;this.assignedAtMillis=assignedAtMillis;}
 public String getEvidenceReference(){return evidenceReference;} public String getReviewerId(){return reviewerId;} public String getAssignedBy(){return assignedBy;} public long getAssignedAtMillis(){return assignedAtMillis;}
 // TODO(Supabase): validate reviewer permissions before shared assignments.
}