package com.alpharip.staffmod.appealconflictdetector;

/**
 * Describes how a user is connected to an appeal case.
 * Which types count as conflicts is configured separately by AppealConflictPolicy.
 */
public enum AppealInvolvementType {
    APPELLANT,
    CASE_SUBJECT,
    ORIGINAL_ACTION_ISSUER,
    PRIOR_REVIEWER,
    WITNESS,
    OTHER
}
