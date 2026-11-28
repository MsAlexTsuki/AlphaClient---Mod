package com.alpharip.staffmod.indicators.bow;

/** Immutable settings for the Bow Charge Indicator. */
public final class BowChargeConfig {
    private final int fullChargeTicks;
    private final int readyPercent;

    public BowChargeConfig() { this(20, 100); }

    public BowChargeConfig(int fullChargeTicks, int readyPercent) {
        if (fullChargeTicks <= 0) throw new IllegalArgumentException("fullChargeTicks must be positive.");
        if (readyPercent < 1 || readyPercent > 100) throw new IllegalArgumentException("readyPercent must be between 1 and 100.");
        this.fullChargeTicks = fullChargeTicks;
        this.readyPercent = readyPercent;
    }

    public int getFullChargeTicks() { return fullChargeTicks; }
    public int getReadyPercent() { return readyPercent; }
}
