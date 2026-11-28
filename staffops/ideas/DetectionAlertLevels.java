package com.alpharip.staffmod.staffops.ideas;

/** Defines ordered alert severity levels for detection results. */
public enum DetectionAlertLevels {
    INFO(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int severity;

    DetectionAlertLevels(int severity) {
        this.severity = severity;
    }

    public int getSeverity() {
        return severity;
    }

    public boolean isAtLeast(DetectionAlertLevels minimum) {
        return minimum != null && severity >= minimum.severity;
    }
}
