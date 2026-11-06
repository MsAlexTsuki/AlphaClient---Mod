package com.alpharip.staffmod.predictions.enderpearl;

import java.util.ArrayList;
import java.util.List;

/** Simulates an Ender Pearl arc and optionally asks a caller-provided probe about collisions. */
public final class EnderPearlTrajectoryPredictor {
    public interface CollisionProbe {
        /** Return true when the segment from start to end intersects a landing surface or obstacle. */
        boolean isCollision(EnderPearlVector start, EnderPearlVector end);
    }

    private final EnderPearlPredictionConfig config;

    public EnderPearlTrajectoryPredictor(EnderPearlPredictionConfig config) {
        if (config == null) throw new IllegalArgumentException("config");
        this.config = config;
    }

    public EnderPearlPredictionResult predict(EnderPearlVector start,
                                               EnderPearlVector initialMotion,
                                               CollisionProbe collisionProbe) {
        if (start == null) throw new IllegalArgumentException("start");
        if (initialMotion == null) throw new IllegalArgumentException("initialMotion");

        List<EnderPearlVector> points = new ArrayList<EnderPearlVector>();
        EnderPearlVector position = start;
        EnderPearlVector motion = initialMotion;
        points.add(position);

        for (int tick = 1; tick <= config.getMaxTicks(); tick++) {
            EnderPearlVector next = position.add(motion);
            points.add(next);

            if (collisionProbe != null && collisionProbe.isCollision(position, next)) {
                return new EnderPearlPredictionResult(points, next, tick,
                        EnderPearlPredictionResult.Status.COLLISION_DETECTED, 0.90D);
            }

            position = next;
            motion = new EnderPearlVector(
                    motion.x * config.getAirDrag(),
                    motion.y * config.getAirDrag() - config.getGravity(),
                    motion.z * config.getAirDrag());
        }

        return new EnderPearlPredictionResult(points, position, config.getMaxTicks(),
                EnderPearlPredictionResult.Status.HORIZON_REACHED, 0.25D);
    }
}
