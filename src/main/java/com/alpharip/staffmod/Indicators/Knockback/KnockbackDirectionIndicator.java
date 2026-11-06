package com.alpharip.staffmod.indicators.knockback;

/**
 * Local prototype for tracking the horizontal direction of received knockback.
 *
 * A future Forge event adapter should pass the player's horizontal knockback
 * motion vector to updateDirection. This class does not read Minecraft events
 * or render a HUD indicator by itself.
 */
public final class KnockbackDirectionIndicator {
    public static final String NAME = "Knockback Direction Indicator";
    public static final String CATEGORY = "Indicators";
    public static final String STATUS = "PROTOTYPE";
    public static final boolean REQUIRES_SUPABASE = false;
    public static final boolean REQUIRES_SERVER_SUPPORT = false;

    private final KnockbackIndicatorConfig config;
    private KnockbackDirectionSnapshot latestSnapshot;

    public KnockbackDirectionIndicator() {
        this(new KnockbackIndicatorConfig());
    }

    public KnockbackDirectionIndicator(KnockbackIndicatorConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null.");
        }
        this.config = config;
    }

    /**
     * Records a horizontal knockback motion vector using the current system time.
     * The vector is expected to describe the direction the player is pushed.
     */
    public synchronized void updateDirection(double x, double z) {
        updateDirection(x, z, System.currentTimeMillis());
    }

    /**
     * Records a horizontal knockback motion vector with an explicit timestamp.
     * This overload allows deterministic testing without Minecraft integration.
     */
    public synchronized void updateDirection(double x, double z, long timestampMillis) {
        if (Double.isNaN(x) || Double.isInfinite(x)
                || Double.isNaN(z) || Double.isInfinite(z)) {
            throw new IllegalArgumentException("Direction values must be finite.");
        }

        double magnitude = Math.sqrt((x * x) + (z * z));
        if (magnitude < config.getMinimumMagnitude()) {
            latestSnapshot = null;
            return;
        }

        latestSnapshot = new KnockbackDirectionSnapshot(
                x, z, x / magnitude, z / magnitude, magnitude, timestampMillis);
    }

    /** Returns whether a recent knockback direction is available. */
    public synchronized boolean isVisible() {
        return getSnapshot() != null;
    }

    /** Returns the most recent non-expired snapshot, or null when unavailable. */
    public synchronized KnockbackDirectionSnapshot getSnapshot() {
        if (latestSnapshot == null) {
            return null;
        }

        long age = Math.max(0L, System.currentTimeMillis()
                - latestSnapshot.getTimestampMillis());
        if (age > config.getVisibleDurationMillis()) {
            latestSnapshot = null;
            return null;
        }
        return latestSnapshot;
    }

    /** Raw horizontal X component of the latest non-expired knockback vector. */
    public synchronized double getDirectionX() {
        KnockbackDirectionSnapshot snapshot = getSnapshot();
        return snapshot == null ? 0.0D : snapshot.getDirectionX();
    }

    /** Raw horizontal Z component of the latest non-expired knockback vector. */
    public synchronized double getDirectionZ() {
        KnockbackDirectionSnapshot snapshot = getSnapshot();
        return snapshot == null ? 0.0D : snapshot.getDirectionZ();
    }

    public synchronized void clear() {
        latestSnapshot = null;
    }

    public KnockbackIndicatorConfig getConfig() {
        return config;
    }
}
