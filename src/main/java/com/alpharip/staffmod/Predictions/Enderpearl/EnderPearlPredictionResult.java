package com.alpharip.staffmod.predictions.enderpearl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable trajectory result. Confidence is a heuristic, not a calibrated probability. */
public final class EnderPearlPredictionResult {
    public enum Status {
        COLLISION_DETECTED,
        HORIZON_REACHED
    }

    private final List<EnderPearlVector> trajectory;
    private final EnderPearlVector predictedLandingPosition;
    private final int simulatedTicks;
    private final Status status;
    private final double confidence;

    public EnderPearlPredictionResult(List<EnderPearlVector> trajectory,
                                      EnderPearlVector predictedLandingPosition,
                                      int simulatedTicks, Status status, double confidence) {
        if (trajectory == null || trajectory.isEmpty()) throw new IllegalArgumentException("trajectory");
        if (predictedLandingPosition == null) throw new IllegalArgumentException("predictedLandingPosition");
        if (simulatedTicks < 1) throw new IllegalArgumentException("simulatedTicks");
        if (status == null) throw new IllegalArgumentException("status");
        if (Double.isNaN(confidence) || Double.isInfinite(confidence)
                || confidence < 0.0D || confidence > 1.0D) {
            throw new IllegalArgumentException("confidence must be between 0 and 1.");
        }
        this.trajectory = Collections.unmodifiableList(new ArrayList<EnderPearlVector>(trajectory));
        this.predictedLandingPosition = predictedLandingPosition;
        this.simulatedTicks = simulatedTicks;
        this.status = status;
        this.confidence = confidence;
    }

    public List<EnderPearlVector> getTrajectory() { return trajectory; }
    public EnderPearlVector getPredictedLandingPosition() { return predictedLandingPosition; }
    public int getSimulatedTicks() { return simulatedTicks; }
    public Status getStatus() { return status; }
    public boolean isCollisionDetected() { return status == Status.COLLISION_DETECTED; }
    /** Heuristic confidence: collision-probed endpoint is stronger than a simulation cutoff. */
    public double getConfidence() { return confidence; }
}
