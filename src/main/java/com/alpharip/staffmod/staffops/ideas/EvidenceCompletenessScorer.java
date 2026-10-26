package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Evidence Completeness Scorer.
 * Scores whether a case contains its required evidence checklist.
 * This is a local advisory only; it must never decide guilt or apply sanctions.
 * TODO(Supabase): load approved evidence templates when the owner adds the integration.
 */
public final class EvidenceCompletenessScorer {
    public static final String NAME = "Evidence Completeness Scorer";
    public static final String CATEGORY = "Trial Moderator";
    public static final String STATUS = "NEW";
    public static final boolean REQUIRES_SUPABASE = true;
    public static final boolean REQUIRES_SERVER_SUPPORT = false;
    private final List<String> requiredEvidence;

    public EvidenceCompletenessScorer(List<String> requiredEvidence) {
        if (requiredEvidence == null) throw new IllegalArgumentException("requiredEvidence");
        this.requiredEvidence = Collections.unmodifiableList(new ArrayList<String>(requiredEvidence));
    }

    public List<String> getRequiredEvidence() { return requiredEvidence; }

    public int score(List<String> suppliedEvidence) {
        if (requiredEvidence.isEmpty()) return 100;
        int matched = 0;
        if (suppliedEvidence != null) {
            for (String required : requiredEvidence) {
                for (String supplied : suppliedEvidence) {
                    if (required.equalsIgnoreCase(supplied)) { matched++; break; }
                }
            }
        }
        return Math.min(100, matched * 100 / requiredEvidence.size());
    }
}
