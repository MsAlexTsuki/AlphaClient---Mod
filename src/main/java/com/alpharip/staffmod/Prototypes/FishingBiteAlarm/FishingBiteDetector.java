package com.alpharip.staffmod.fishingbitealarm;

/**
 * Detects a sharp downward bobber impulse while the hook is in water.
 *
 * This is a client-side motion heuristic, not a server-confirmed fishing-bite event.
 * Thresholds must be validated in Minecraft 1.8.9 before production use.
 */
public final class FishingBiteDetector {
    private static final double BITE_MOTION_THRESHOLD = -0.12D;
    private static final double PREVIOUS_MOTION_CEILING = -0.04D;
    private static final double MINIMUM_MOTION_DROP = 0.08D;

    private int trackedHookId = Integer.MIN_VALUE;
    private double previousY;
    private double previousMotionY;
    private boolean hasPreviousSample;

    public boolean update(FishingHookSample sample) {
        if (sample == null) {
            reset();
            return false;
        }

        if (sample.getEntityId() != trackedHookId) {
            trackedHookId = sample.getEntityId();
            previousY = sample.getY();
            previousMotionY = sample.getMotionY();
            hasPreviousSample = true;
            return false;
        }

        boolean biteImpulse = sample.isInWater()
                && hasPreviousSample
                && sample.getMotionY() <= BITE_MOTION_THRESHOLD
                && previousMotionY > PREVIOUS_MOTION_CEILING
                && sample.getMotionY() < previousMotionY - MINIMUM_MOTION_DROP
                && sample.getY() < previousY;

        previousY = sample.getY();
        previousMotionY = sample.getMotionY();
        hasPreviousSample = true;
        return biteImpulse;
    }

    public void reset() {
        trackedHookId = Integer.MIN_VALUE;
        previousY = 0.0D;
        previousMotionY = 0.0D;
        hasPreviousSample = false;
    }
}
