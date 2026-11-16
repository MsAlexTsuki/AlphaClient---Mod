package com.alpharip.staffmod.blindcasereview;

import java.util.List;

/** The only case data exposed to a reviewer before a decision is submitted. */
public final class BlindCaseView {
    private final String caseReference;
    private final String reviewSummary;
    private final List<BlindCaseEvidence> evidence;

    BlindCaseView(String caseReference, String reviewSummary, List<BlindCaseEvidence> evidence) {
        this.caseReference = caseReference;
        this.reviewSummary = reviewSummary;
        this.evidence = evidence;
    }

    public String getCaseReference() { return caseReference; }
    public String getReviewSummary() { return reviewSummary; }
    public List<BlindCaseEvidence> getEvidence() { return evidence; }
}
