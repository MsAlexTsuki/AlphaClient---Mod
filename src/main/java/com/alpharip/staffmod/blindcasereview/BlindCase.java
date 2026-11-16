package com.alpharip.staffmod.blindcasereview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A case split into reviewer-facing material and context withheld until a
 * decision has been submitted. The caller is responsible for that separation.
 */
public final class BlindCase {
    private final String caseReference;
    private final String reviewSummary;
    private final List<BlindCaseEvidence> evidence;
    private final Map<String, String> restrictedContext;

    public BlindCase(String caseReference, String reviewSummary,
                     List<BlindCaseEvidence> evidence, Map<String, String> restrictedContext) {
        this.caseReference = requireText(caseReference, "caseReference");
        this.reviewSummary = requireText(reviewSummary, "reviewSummary");
        if (evidence == null) throw new IllegalArgumentException("evidence is required");
        if (restrictedContext == null) throw new IllegalArgumentException("restrictedContext is required");

        this.evidence = Collections.unmodifiableList(new ArrayList<BlindCaseEvidence>(evidence));
        Map<String, String> contextCopy = new LinkedHashMap<String, String>();
        for (Map.Entry<String, String> entry : restrictedContext.entrySet()) {
            if (entry.getKey() == null || entry.getKey().trim().isEmpty()) continue;
            contextCopy.put(entry.getKey(), entry.getValue() == null ? "" : entry.getValue());
        }
        this.restrictedContext = Collections.unmodifiableMap(contextCopy);
    }

    public String getCaseReference() { return caseReference; }
    public String getReviewSummary() { return reviewSummary; }
    public List<BlindCaseEvidence> getEvidence() { return evidence; }
    Map<String, String> getRestrictedContext() { return restrictedContext; }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        return value.trim();
    }
}
