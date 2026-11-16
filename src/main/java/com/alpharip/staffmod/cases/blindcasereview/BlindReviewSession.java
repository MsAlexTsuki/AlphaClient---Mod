package com.alpharip.staffmod.blindcasereview;

import java.util.Collections;
import java.util.Map;

/** Controls the decision-before-context-reveal workflow for one case. */
public final class BlindReviewSession {
    private final String sessionId;
    private final BlindCase blindCase;
    private final BlindCaseView reviewerView;
    private BlindReviewDecision decision;
    private boolean contextRevealed;

    BlindReviewSession(String sessionId, BlindCase blindCase, BlindCaseAnonymizer anonymizer) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("sessionId must not be empty");
        }
        if (blindCase == null || anonymizer == null) {
            throw new IllegalArgumentException("case and anonymizer are required");
        }
        this.sessionId = sessionId.trim();
        this.blindCase = blindCase;
        this.reviewerView = anonymizer.createReviewerView(blindCase);
    }

    public String getSessionId() { return sessionId; }
    public BlindCaseView getReviewerView() { return reviewerView; }
    public synchronized boolean hasDecision() { return decision != null; }
    public synchronized BlindReviewDecision getDecision() { return decision; }
    public synchronized boolean isContextRevealed() { return contextRevealed; }

    public synchronized void submitDecision(BlindReviewDecision decision) {
        if (decision == null) throw new IllegalArgumentException("decision is required");
        if (this.decision != null) throw new IllegalStateException("A decision has already been submitted");
        this.decision = decision;
    }

    public synchronized Map<String, String> revealRestrictedContext() {
        if (decision == null) {
            throw new IllegalStateException("Submit a decision before revealing restricted context");
        }
        contextRevealed = true;
        return blindCase.getRestrictedContext();
    }

    public synchronized Map<String, String> getRevealedContext() {
        return contextRevealed ? blindCase.getRestrictedContext() : Collections.<String, String>emptyMap();
    }
}
