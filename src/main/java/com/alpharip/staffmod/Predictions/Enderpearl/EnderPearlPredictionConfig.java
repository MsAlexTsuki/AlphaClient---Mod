package com.alpharip.staffmod.predictions.enderpearl;

/** Immutable physics settings for the local Ender Pearl trajectory prototype. */
public final class EnderPearlPredictionConfig {
    private final double gravity;
    private final double airDrag;
    private final int maxTicks;

    public EnderPearlPredictionConfig(double gravity, double airDrag, int maxTicks) {
        if (Double.isNaN(gravity) || Double.isInfinite(gravity) || gravity < 0.0D) {
            throw new IllegalArgumentException("gravity must be finite and non-negative.");
        }
        if (Double.isNaN(airDrag) || Double.isInfinite(airDrag)
                || airDrag <= 0.0D || airDrag > 1.0D) {
            throw new IllegalArgumentException("airDrag must be greater than 0 and at most 1.");
        }
        if (maxTicks < 1 || maxTicks > 1200) {
            throw new IllegalArgumentException("maxTicks must be between 1 and 1200.");
        }
        this.gravity = gravity;
        this.airDrag = airDrag;
        this.maxTicks = maxTicks;
    }

    public static EnderPearlPredictionConfig defaults() {
        return new EnderPearlPredictionConfig(0.03D, 0.99D, 120);
    }

    public double getGravity() { return gravity; }
    public double getAirDrag() { return airDrag; }
    public int getMaxTicks() { return maxTicks; }
}
