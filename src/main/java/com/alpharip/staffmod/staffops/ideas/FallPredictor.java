package com.alpharip.staffmod.staffops.ideas;
/** Fall-damage estimate helper; actual fall distance and equipment data must be supplied by callers. */
public final class FallPredictor {
    public double estimateDamage(double fallDistance, int featherFallingLevel) {
        double baseDamage = Math.max(0D, fallDistance - 3D);
        int level = Math.max(0, Math.min(4, featherFallingLevel));
        return Math.max(0D, baseDamage * (1D - level * 0.12D));
    }
}
