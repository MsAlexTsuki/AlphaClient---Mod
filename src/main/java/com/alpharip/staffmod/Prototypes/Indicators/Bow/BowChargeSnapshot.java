package com.alpharip.staffmod.indicators.bow;

/** Immutable point-in-time bow charge data for a future HUD renderer. */
public final class BowChargeSnapshot {
    private final int drawTicks;
    private final int chargePercent;
    private final boolean ready;
    private final long timestampMillis;

    public BowChargeSnapshot(int drawTicks, int chargePercent, boolean ready, long timestampMillis) {
        if (drawTicks < 0) throw new IllegalArgumentException("drawTicks cannot be negative.");
        if (chargePercent < 0 || chargePercent > 100) throw new IllegalArgumentException("chargePercent must be between 0 and 100.");
        this.drawTicks = drawTicks;
        this.chargePercent = chargePercent;
        this.ready = ready;
        this.timestampMillis = timestampMillis;
    }

    public int getDrawTicks() { return drawTicks; }
    public int getChargePercent() { return chargePercent; }
    public boolean isReady() { return ready; }
    public long getTimestampMillis() { return timestampMillis; }
}
