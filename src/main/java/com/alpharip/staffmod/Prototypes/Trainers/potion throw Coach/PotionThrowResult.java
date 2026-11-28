package com.alpharip.staffmod.trainers.potionthrowcoach;

/** Immutable measurements for one completed splash-potion attempt. */
public final class PotionThrowResult {
    private final long id;
    private final long reactionTimeMillis;
    private final long flightTimeMillis;
    private final double landingOffset;
    private final boolean accurate;
    private final Position throwOrigin;
    private final Position landingPosition;
    private final Position playerFeetAtLanding;

    PotionThrowResult(long id, long reactionTimeMillis, long flightTimeMillis,
                      double landingOffset, boolean accurate, Position throwOrigin,
                      Position landingPosition, Position playerFeetAtLanding) {
        this.id = id;
        this.reactionTimeMillis = reactionTimeMillis;
        this.flightTimeMillis = flightTimeMillis;
        this.landingOffset = landingOffset;
        this.accurate = accurate;
        this.throwOrigin = throwOrigin;
        this.landingPosition = landingPosition;
        this.playerFeetAtLanding = playerFeetAtLanding;
    }

    public long getId() { return id; }
    /** Returns -1 when no preceding damage event was available. */
    public long getReactionTimeMillis() { return reactionTimeMillis; }
    public long getFlightTimeMillis() { return flightTimeMillis; }
    /** Horizontal X/Z distance between landing point and player feet at landing. */
    public double getLandingOffset() { return landingOffset; }
    public boolean isAccurate() { return accurate; }
    public Position getThrowOrigin() { return throwOrigin; }
    public Position getLandingPosition() { return landingPosition; }
    public Position getPlayerFeetAtLanding() { return playerFeetAtLanding; }

    /** Simple immutable world position; coordinates are supplied by a future Forge adapter. */
    public static final class Position {
        private final double x;
        private final double y;
        private final double z;

        public Position(double x, double y, double z) {
            if (!isFinite(x) || !isFinite(y) || !isFinite(z)) {
                throw new IllegalArgumentException("position coordinates must be finite.");
            }
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public double getX() { return x; }
        public double getY() { return y; }
        public double getZ() { return z; }

        public double horizontalDistanceTo(Position other) {
            if (other == null) {
                throw new IllegalArgumentException("other must not be null.");
            }
            double dx = x - other.x;
            double dz = z - other.z;
            return Math.sqrt(dx * dx + dz * dz);
        }

        private static boolean isFinite(double value) {
            return !Double.isNaN(value) && !Double.isInfinite(value);
        }
    }
}
