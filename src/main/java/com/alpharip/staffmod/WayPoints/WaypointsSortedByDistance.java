package com.alpharip.staffmod.WayPoints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Returns active-profile waypoints in a configurable order, with favorites pinned first. */
public final class WaypointsSortedByDistance {
    public boolean enabled = true;
    public boolean nearestFirst = true;
    public boolean includeOtherDimensions = false;

    public List<StaffWaypoint> sort(List<StaffWaypoint> input, final String server, final int dimension,
                                    final double x, final double y, final double z) {
        return sort(input,server,null,dimension,x,y,z,"distance");
    }

    public List<StaffWaypoint> sort(List<StaffWaypoint> input, final String server, final String profile,
                                    final int dimension, final double x, final double y, final double z,
                                    final String mode) {
        List<StaffWaypoint> result = new ArrayList<StaffWaypoint>();
        if (input != null) for (StaffWaypoint point : input) {
            if (point == null || !server.equals(point.getServerKey())) continue;
            if (profile != null && !profile.equalsIgnoreCase(point.getProfileId())) continue;
            if (!includeOtherDimensions && point.getDimensionId() != dimension) continue;
            result.add(point);
        }
        final String sortMode=mode==null?"distance":mode.toLowerCase(java.util.Locale.ROOT);
        Collections.sort(result, new Comparator<StaffWaypoint>() {
            public int compare(StaffWaypoint a, StaffWaypoint b) {
                if (a.isFavorite()!=b.isFavorite()) return a.isFavorite()?-1:1;
                int comparison=0;
                if ("name".equals(sortMode)) comparison=a.getLabel().compareToIgnoreCase(b.getLabel());
                else if ("category".equals(sortMode)) comparison=a.getCategory().compareToIgnoreCase(b.getCategory());
                else if ("modified".equals(sortMode)) comparison=Long.compare(b.getLastModified(),a.getLastModified());
                else {
                    double ad=distanceSq(a,x,y,z),bd=distanceSq(b,x,y,z);
                    comparison=nearestFirst?Double.compare(ad,bd):Double.compare(bd,ad);
                }
                if(comparison==0) comparison=a.getLabel().compareToIgnoreCase(b.getLabel());
                return comparison;
            }
        });
        return Collections.unmodifiableList(result);
    }

    private static double distanceSq(StaffWaypoint p, double x, double y, double z) {
        double dx = p.getX() - x, dy = p.getY() - y, dz = p.getZ() - z;
        return dx * dx + dy * dy + dz * dz;
    }
}
