package com.alpharip.staffmod.staffops.ideas;

/** Represents a normalized detection sensitivity value between 0 and 1. */
public class DetectionSensitivity {
    private double value = 0.5D;

    public DetectionSensitivity() {
    }

    public DetectionSensitivity(double value) {
        setValue(value);
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("value must be finite");
        }
        this.value = Math.max(0.0D, Math.min(1.0D, value));
    }
}
