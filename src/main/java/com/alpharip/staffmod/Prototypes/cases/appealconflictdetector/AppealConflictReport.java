package com.alpharip.staffmod.appealconflictdetector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable result for one appeal case. */
public final class AppealConflictReport {
    public enum Status {
        NO_REVIEWER_ASSIGNED,
        NO_CONFLICT_FOUND,
        CONFLICTS_FOUND
    }

    private final String caseId;
    private final String reviewerId;
    private final List<AppealConflictFinding> findings;
    private final Status status;

    public AppealConflictReport(String caseId, String reviewerId, List<AppealConflictFinding> findings) {
        this.caseId = caseId;
        this.reviewerId = reviewerId == null ? "" : reviewerId;
        List<AppealConflictFinding> copy = findings == null
                ? new ArrayList<AppealConflictFinding>()
                : new ArrayList<AppealConflictFinding>(findings);
        this.findings = Collections.unmodifiableList(copy);
        if (this.reviewerId.isEmpty()) status = Status.NO_REVIEWER_ASSIGNED;
        else if (copy.isEmpty()) status = Status.NO_CONFLICT_FOUND;
        else status = Status.CONFLICTS_FOUND;
    }

    public String getCaseId() { return caseId; }
    public String getReviewerId() { return reviewerId; }
    public List<AppealConflictFinding> getFindings() { return findings; }
    public Status getStatus() { return status; }
    public boolean hasConflicts() { return !findings.isEmpty(); }
}
