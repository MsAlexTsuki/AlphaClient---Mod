package com.alpharip.staffmod.staffops.ideas;

/** Holds the selected review mode for detections. */
public class DetectionReviewMode {
    public enum Mode {
        MANUAL,
        ASSISTED,
        READ_ONLY
    }

    private Mode mode = Mode.MANUAL;

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("mode cannot be null");
        }
        this.mode = mode;
    }

    public boolean allowsManualDecisions() {
        return mode != Mode.READ_ONLY;
    }
}
