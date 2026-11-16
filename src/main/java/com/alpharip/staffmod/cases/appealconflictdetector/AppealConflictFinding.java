package com.alpharip.staffmod.appealconflictdetector;

/** One policy match between the assigned reviewer and a case participant. */
public final class AppealConflictFinding {
    private final String caseId;
    private final String reviewerId;
    private final String involvedUserId;
    private final AppealInvolvementType involvementType;
    private final String ruleDescription;
    private final String reference;

    public AppealConflictFinding(String caseId, String reviewerId, AppealInvolvement involvement,
                                 AppealConflictRule rule) {
        if (caseId == null || reviewerId == null || involvement == null || rule == null) {
            throw new IllegalArgumentException("finding fields are required");
        }
        this.caseId = caseId;
        this.reviewerId = reviewerId;
        this.involvedUserId = involvement.getUserId();
        this.involvementType = involvement.getType();
        this.ruleDescription = rule.getDescription();
        this.reference = involvement.getReference();
    }

    public String getCaseId() { return caseId; }
    public String getReviewerId() { return reviewerId; }
    public String getInvolvedUserId() { return involvedUserId; }
    public AppealInvolvementType getInvolvementType() { return involvementType; }
    public String getRuleDescription() { return ruleDescription; }
    public String getReference() { return reference; }
}
