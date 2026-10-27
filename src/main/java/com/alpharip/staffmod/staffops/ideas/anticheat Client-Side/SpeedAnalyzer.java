package com.alpharip.staffmod.staffops.ideas;

/** Calculates speed between two supplied position samples; no live player data is read. */
public final class SpeedAnalyzer {
    public double blocksPerSecond(double x1, double y1, double z1,
                                  double x2, double y2, double z2,
                                  long elapsedMillis) {
        if (elapsedMillis <= 0L) return 0.0D;
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        return distance * 1000.0D / elapsedMillis;
    }
}