package com.alpharip.staffmod.staffops.ideas;
/** Direction estimate only; sprint state, server rules and preview rendering are pending. */
public final class KnockbackPreview {
    public static final class Direction {
        public final double x, z;
        public Direction(double x, double z) { this.x = x; this.z = z; }
    }
    public Direction fromYaw(double yawDegrees) {
        double radians = Math.toRadians(yawDegrees);
        return new Direction(-Math.sin(radians), Math.cos(radians));
    }
}
