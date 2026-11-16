package com.alpharip.staffmod.blindcasereview;

/**
 * Builds the blind reviewer view from fields explicitly supplied as review
 * material. It does not detect or remove names, identifiers, or other PII.
 */
public final class BlindCaseAnonymizer {
    public BlindCaseView createReviewerView(BlindCase blindCase) {
        if (blindCase == null) throw new IllegalArgumentException("blindCase is required");
        return new BlindCaseView(
                blindCase.getCaseReference(),
                blindCase.getReviewSummary(),
                blindCase.getEvidence()
        );
    }
}
