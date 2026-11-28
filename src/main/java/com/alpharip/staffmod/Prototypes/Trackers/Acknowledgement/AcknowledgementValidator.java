package com.alpharip.staffmod.trackers.acknowledgement;

/** Input validation shared by the acknowledgement service and registry. */
public final class AcknowledgementValidator {
    public static final int MAX_NAME_LENGTH = 32;
    public static final int MAX_SUBJECT_LENGTH = 100;
    public static final int MAX_DETAILS_LENGTH = 500;

    private AcknowledgementValidator() { }

    public static boolean isValid(String createdBy, String subject, String details) {
        return validLength(createdBy, 1, MAX_NAME_LENGTH)
            && validLength(subject, 1, MAX_SUBJECT_LENGTH)
            && (details == null || details.trim().length() <= MAX_DETAILS_LENGTH);
    }

    private static boolean validLength(String value, int min, int max) {
        if (value == null) {
            return false;
        }
        int length = value.trim().length();
        return length >= min && length <= max;
    }
}
