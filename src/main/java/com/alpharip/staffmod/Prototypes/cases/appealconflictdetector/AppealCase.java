package com.alpharip.staffmod.appealconflictdetector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable appeal review input. The caller supplies all case and involvement data. */
public final class AppealCase {
    private final String caseId;
    private final String assignedReviewerId;
    private final List<AppealInvolvement> involvements;

    public AppealCase(String caseId, String assignedReviewerId, List<AppealInvolvement> involvements) {
        if (caseId == null || caseId.trim().isEmpty()) {
            throw new IllegalArgumentException("caseId is required");
        }
        this.caseId = caseId.trim();
        this.assignedReviewerId = assignedReviewerId == null ? "" : assignedReviewerId.trim();
        List<AppealInvolvement> copy = new ArrayList<AppealInvolvement>();
        if (involvements != null) {
            for (AppealInvolvement involvement : involvements) {
                if (involvement != null) copy.add(involvement);
            }
        }
        this.involvements = Collections.unmodifiableList(copy);
    }

    public String getCaseId() { return caseId; }
    public String getAssignedReviewerId() { return assignedReviewerId; }
    public boolean hasAssignedReviewer() { return !assignedReviewerId.isEmpty(); }
    public List<AppealInvolvement> getInvolvements() { return involvements; }
}
