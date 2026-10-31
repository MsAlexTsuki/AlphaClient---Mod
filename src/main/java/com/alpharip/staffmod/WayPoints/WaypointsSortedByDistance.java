package com.alpharip.staffmod.WayPoints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Returns waypoints in nearest-first order for the current dimension. */
public final class WaypointsSortedByDistance {
    public boolean enabled = true;
    public boolean nearestFirst = true;
    public boolean includeOtherDimensions = false;

    public List<StaffWaypoint> sort(List<StaffWaypoint> input, final String server, final int dimension,
                                    final double x, final double y, final double z) {
        List<StaffWaypoint> result = new ArrayList<StaffWaypoint>();
        if (input != null) for (StaffWaypoint point : input) {
            if (point == null || !server.equals(point.getServerKey())) continue;
            if (!includeOtherDimensions && point.getDimensionId() != dimension) continue;
            result.add(point);
        }
        Collections.sort(result, new Comparator<StaffWaypoint>() {
            public int compare(StaffWaypoint a, StaffWaypoint b) {
                int da = a.getDimensionId() == dimension ? 0 : 1;
                int db = b.getDimensionId() == dimension ? 0 : 1;
                if (da != db) return da - db;
                double ad = distanceSq(a, x, y, z), bd = distanceSq(b, x, y, z);
                return nearestFirst ? Double.compare(ad, bd) : Double.compare(bd, ad);
            }
        });
        return Collections.unmodifiableList(result);
    }

    private static double distanceSq(StaffWaypoint p, double x, double y, double z) {
        double dx = p.getX() - x, dy = p.getY() - y, dz = p.getZ() - z;
        return dx * dx + dy * dy + dz * dz;
    }
}
