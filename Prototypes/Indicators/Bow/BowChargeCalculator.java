package com.alpharip.staffmod.indicators.bow;

/** Pure calculation using Minecraft's vanilla bow draw curve. */
public final class BowChargeCalculator {
    private BowChargeCalculator() { }

    /** Vanilla bow charge reaches 100 percent after 20 draw ticks. */
    public static int calculatePercent(int drawTicks, int fullChargeTicks) {
        if (fullChargeTicks <= 0) throw new IllegalArgumentException("fullChargeTicks must be positive.");
        double draw = Math.max(0, drawTicks) / (double) fullChargeTicks;
        double strength = (draw * draw + 2.0D * draw) / 3.0D;
        strength = Math.max(0.0D, Math.min(1.0D, strength));
        return (int) Math.round(strength * 100.0D);
    }
}
