package com.alpharip.staffmod.aimpatternlibrary;

/** Central validation for user-supplied pattern metadata and observations. */
public final class AimPatternValidator {
    public static final int MAX_NAME_LENGTH = 40;
    private AimPatternValidator() { }

    public static String validateName(String name) {
        if (name == null) throw new IllegalArgumentException("Pattern name cannot be null");
        String value = name.trim();
        if (value.isEmpty() || value.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Pattern name must contain 1-" + MAX_NAME_LENGTH + " characters");
        }
        return value;
    }
    public static void validateObservation(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("Observation must be finite");
        }
    }
}
