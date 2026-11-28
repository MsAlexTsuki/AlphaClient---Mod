package com.alpharip.staffmod.trainers.enderpearl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Ender Pearl Accuracy Practice prototype.
 *
 * This trainer evaluates where a thrown pearl actually lands against a practice
 * target. A future Minecraft/Forge adapter must supply throw and landing data.
 * It intentionally does not simulate trajectories; that belongs to the separate
 * Ender Pearl Landing Prediction module.
 */
public final class EnderPearlAccuracyPractice {
    public static final String NAME = "Ender Pearl Accuracy Practice";
    public static final String CATEGORY = "Practice";
    public static final String STATUS = "PROTOTYPE";
    public static final boolean REQUIRES_SUPABASE = false;
    public static final boolean REQUIRES_SERVER_SUPPORT = false;

    private final Config config;
    private final List<ThrowResult> history = new ArrayList<ThrowResult>();
    private Target activeTarget;
    private PendingThrow pendingThrow;
    private long nextThrowId = 1L;
    private int totalThrows;
    private int accurateThrows;
    private int currentStreak;
    private int bestStreak;

    public EnderPearlAccuracyPractice() {
        this(new Config());
    }

    public EnderPearlAccuracyPractice(Config config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null.");
        }
        this.config = config;
    }

    /** Sets the point the player is practising to land on. */
    public synchronized void setTarget(double x, double y, double z) {
        activeTarget = new Target(x, y, z);
    }

    public synchronized void clearTarget() {
        activeTarget = null;
        pendingThrow = null;
    }

    public synchronized boolean hasTarget() {
        return activeTarget != null;
    }

    public synchronized Target getTarget() {
        return activeTarget;
    }

    /**
     * Starts tracking a pearl throw. The caller supplies the throw origin and
     * timestamp from a future client-side entity/event adapter.
     */
    public synchronized long recordThrow(double originX, double originY,
                                         double originZ, long timestampMillis) {
        if (activeTarget == null) {
            throw new IllegalStateException("Set a practice target before throwing.");
        }
        if (pendingThrow != null) {
            throw new IllegalStateException("The previous pearl has not landed or been cancelled.");
        }
        pendingThrow = new PendingThrow(nextThrowId++, activeTarget,
                new Target(originX, originY, originZ), timestampMillis);
        totalThrows++;
        return pendingThrow.id;
    }

    /**
     * Completes the active throw with the actual landing position supplied by
     * the future runtime adapter. Accuracy is based on 3D distance to the target.
     */
    public synchronized ThrowResult recordLanding(double x, double y, double z,
                                                   long timestampMillis) {
        if (pendingThrow == null) {
            throw new IllegalStateException("There is no tracked pearl throw.");
        }

        Target landing = new Target(x, y, z);
        double distance = pendingThrow.target.distanceTo(landing);
        boolean accurate = distance <= config.getAccuracyRadius();
        long duration = Math.max(0L, timestampMillis - pendingThrow.timestampMillis);

        ThrowResult result = new ThrowResult(pendingThrow.id, pendingThrow.target,
                pendingThrow.origin, landing, distance, accurate, duration);
        history.add(result);
        trimHistory();

        if (accurate) {
            accurateThrows++;
            currentStreak++;
            bestStreak = Math.max(bestStreak, currentStreak);
        } else {
            currentStreak = 0;
        }

        pendingThrow = null;
        return result;
    }

    /** Cancels the current attempt without changing completed-throw statistics. */
    public synchronized boolean cancelPendingThrow() {
        if (pendingThrow == null) {
            return false;
        }
        pendingThrow = null;
        totalThrows--;
        return true;
    }

    public synchronized boolean hasPendingThrow() {
        return pendingThrow != null;
    }

    public synchronized int getTotalThrows() {
        return totalThrows;
    }

    public synchronized int getCompletedThrows() {
        return history.size();
    }

    public synchronized int getAccurateThrows() {
        return accurateThrows;
    }

    public synchronized int getMissedThrows() {
        return history.size() - accurateThrows;
    }

    public synchronized double getAccuracyPercent() {
        return history.isEmpty() ? 0.0D
                : accurateThrows * 100.0D / history.size();
    }

    public synchronized int getCurrentStreak() {
        return currentStreak;
    }

    public synchronized int getBestStreak() {
        return bestStreak;
    }

    public synchronized double getAverageError() {
        if (history.isEmpty()) {
            return 0.0D;
        }
        double total = 0.0D;
        for (ThrowResult result : history) {
            total += result.getDistanceFromTarget();
        }
        return total / history.size();
    }

    public synchronized ThrowResult getLastResult() {
        return history.isEmpty() ? null : history.get(history.size() - 1);
    }

    public synchronized List<ThrowResult> getHistory() {
        return Collections.unmodifiableList(
                new ArrayList<ThrowResult>(history));
    }

    /** Resets attempts, results, and streaks while keeping the configured target. */
    public synchronized void resetStatistics() {
        pendingThrow = null;
        totalThrows = 0;
        accurateThrows = 0;
        currentStreak = 0;
        bestStreak = 0;
        history.clear();
        nextThrowId = 1L;
    }

    public Config getConfig() {
        return config;
    }

    private void trimHistory() {
        while (history.size() > config.getMaximumHistoryEntries()) {
            history.remove(0);
        }
    }

    public static final class Config {
        private double accuracyRadius = 2.0D;
        private int maximumHistoryEntries = 100;

        public synchronized double getAccuracyRadius() {
            return accuracyRadius;
        }

        public synchronized void setAccuracyRadius(double radius) {
            if (!isFinite(radius) || radius <= 0.0D) {
                throw new IllegalArgumentException("accuracy radius must be finite and > 0.");
            }
            accuracyRadius = radius;
        }

        public synchronized int getMaximumHistoryEntries() {
            return maximumHistoryEntries;
        }

        public synchronized void setMaximumHistoryEntries(int maximum) {
            if (maximum < 1) {
                throw new IllegalArgumentException("maximum history entries must be >= 1.");
            }
            maximumHistoryEntries = maximum;
        }
    }

    public static final class Target {
        private final double x;
        private final double y;
        private final double z;

        public Target(double x, double y, double z) {
            if (!isFinite(x) || !isFinite(y) || !isFinite(z)) {
                throw new IllegalArgumentException("target coordinates must be finite.");
            }
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public double getX() { return x; }
        public double getY() { return y; }
        public double getZ() { return z; }

        public double distanceTo(Target other) {
            if (other == null) {
                throw new IllegalArgumentException("other must not be null.");
            }
            double dx = x - other.x;
            double dy = y - other.y;
            double dz = z - other.z;
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
    }

    public static final class ThrowResult {
        private final long id;
        private final Target target;
        private final Target origin;
        private final Target landing;
        private final double distanceFromTarget;
        private final boolean accurate;
        private final long flightDurationMillis;

        private ThrowResult(long id, Target target, Target origin, Target landing,
                            double distanceFromTarget, boolean accurate,
                            long flightDurationMillis) {
            this.id = id;
            this.target = target;
            this.origin = origin;
            this.landing = landing;
            this.distanceFromTarget = distanceFromTarget;
            this.accurate = accurate;
            this.flightDurationMillis = flightDurationMillis;
        }

        public long getId() { return id; }
        public Target getTarget() { return target; }
        public Target getOrigin() { return origin; }
        public Target getLanding() { return landing; }
        public double getDistanceFromTarget() { return distanceFromTarget; }
        public boolean isAccurate() { return accurate; }
        public long getFlightDurationMillis() { return flightDurationMillis; }
    }

    private static final class PendingThrow {
        private final long id;
        private final Target target;
        private final Target origin;
        private final long timestampMillis;

        private PendingThrow(long id, Target target, Target origin, long timestampMillis) {
            this.id = id;
            this.target = target;
            this.origin = origin;
            this.timestampMillis = timestampMillis;
        }
    }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }
}
