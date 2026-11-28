package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores sampled player positions for a single session route.
 * Minecraft position sampling and route visualization must be integrated later.
 */
public final class SessionRouteHistory {
    private final List<RoutePoint> points = new ArrayList<RoutePoint>();

    public void recordPosition(double x, double y, double z, long timestampMillis) {
        points.add(new RoutePoint(x, y, z, timestampMillis));
    }

    public List<RoutePoint> getPoints() {
        return Collections.unmodifiableList(new ArrayList<RoutePoint>(points));
    }

    public void clear() {
        points.clear();
    }

    public int size() {
        return points.size();
    }

    public static final class RoutePoint {
        private final double x;
        private final double y;
        private final double z;
        private final long timestampMillis;

        public RoutePoint(double x, double y, double z, long timestampMillis) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.timestampMillis = timestampMillis;
        }

        public double getX() { return x; }
        public double getY() { return y; }
        public double getZ() { return z; }
        public long getTimestampMillis() { return timestampMillis; }
    }
}
