package com.alpharip.staffmod.appealconflictdetector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Evaluates a case against an explicitly configured conflict policy. */
public final class AppealConflictDetector {
    public AppealConflictReport evaluate(AppealCase appealCase, AppealConflictPolicy policy) {
        if (appealCase == null) throw new IllegalArgumentException("appealCase is required");
        if (policy == null) throw new IllegalArgumentException("policy is required");

        List<AppealConflictFinding> findings = new ArrayList<AppealConflictFinding>();
        if (appealCase.hasAssignedReviewer()) {
            String reviewerId = normalize(appealCase.getAssignedReviewerId());
            Map<AppealInvolvementType, AppealConflictRule> rules = policy.snapshot();

            for (AppealInvolvement involvement : appealCase.getInvolvements()) {
                if (involvement == null || !reviewerId.equals(normalize(involvement.getUserId()))) continue;
                AppealConflictRule rule = rules.get(involvement.getType());
                if (rule != null && rule.isEnabled()) {
                    findings.add(new AppealConflictFinding(
                            appealCase.getCaseId(), appealCase.getAssignedReviewerId(), involvement, rule));
                }
            }
        }
        return new AppealConflictReport(
                appealCase.getCaseId(), appealCase.getAssignedReviewerId(), findings);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
