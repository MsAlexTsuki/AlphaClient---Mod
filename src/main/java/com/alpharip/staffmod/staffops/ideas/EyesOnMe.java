package com.alpharip.staffmod.staffops.ideas;
/** View-cone geometry helper; player rotation acquisition and indicator rendering are pending. */
public final class EyesOnMe {
    public boolean isWithinYawTolerance(double targetYaw, double viewerYaw, double toleranceDegrees) {
        double difference = Math.abs((targetYaw - viewerYaw) % 360D);
        if (difference > 180D) difference = 360D - difference;
        return difference <= Math.max(0D, toleranceDegrees);
    }
}
