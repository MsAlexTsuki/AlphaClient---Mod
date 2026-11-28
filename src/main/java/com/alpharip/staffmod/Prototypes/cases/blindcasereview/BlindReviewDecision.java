package com.alpharip.staffmod.blindcasereview;

/** Immutable local record of one submitted blind-review decision. */
public final class BlindReviewDecision {
    private final String reviewerReference;
    private final BlindReviewOutcome outcome;
    private final String rationale;
    private final long submittedAt;

    public BlindReviewDecision(String reviewerReference, BlindReviewOutcome outcome, String rationale) {
        if (reviewerReference == null || reviewerReference.trim().isEmpty()) {
            throw new IllegalArgumentException("reviewerReference must not be empty");
        }
        if (outcome == null) throw new IllegalArgumentException("outcome is required");
        if (rationale == null || rationale.trim().isEmpty()) {
            throw new IllegalArgumentException("rationale must not be empty");
        }
        this.reviewerReference = reviewerReference.trim();
        this.outcome = outcome;
        this.rationale = rationale.trim();
        this.submittedAt = System.currentTimeMillis();
    }

    public String getReviewerReference() { return reviewerReference; }
    public BlindReviewOutcome getOutcome() { return outcome; }
    public String getRationale() { return rationale; }
    public long getSubmittedAt() { return submittedAt; }
}
