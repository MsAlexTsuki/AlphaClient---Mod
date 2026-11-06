package com.alpharip.staffmod.trainers.aimtrainer;

/**
 * Immutable configuration for a local Aim Trainer session.
 */
public final class AimTrainerConfig {
    private final int heatmapGridSize;

    public AimTrainerConfig() {
        this(9);
    }

    public AimTrainerConfig(int heatmapGridSize) {
        if (heatmapGridSize < 3 || heatmapGridSize % 2 == 0) {
            throw new IllegalArgumentException("heatmapGridSize must be odd and >= 3.");
        }
        this.heatmapGridSize = heatmapGridSize;
    }

    public int getHeatmapGridSize() {
        return heatmapGridSize;
    }
}
