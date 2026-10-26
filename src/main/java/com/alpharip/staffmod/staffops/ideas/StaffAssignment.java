package com.alpharip.staffmod.staffops.ideas;

/** Local assignment draft for a case; this does not grant permissions or notify staff. */
public final class StaffAssignment {
    private String caseReference = "";
    private String assignee = "";
    private String assignedBy = "";
    private long assignedAtMillis;

    public void set(String caseReference, String assignee, String assignedBy, long assignedAtMillis) {
        this.caseReference = caseReference == null ? "" : caseReference;
        this.assignee = assignee == null ? "" : assignee;
        this.assignedBy = assignedBy == null ? "" : assignedBy;
        this.assignedAtMillis = assignedAtMillis;
    }

    public String getCaseReference() { return caseReference; }
    public String getAssignee() { return assignee; }
    public String getAssignedBy() { return assignedBy; }
    public long getAssignedAtMillis() { return assignedAtMillis; }

    // TODO(Supabase): validate staff access and persist assignments through an authorized backend.
}