package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds waypoint positions and icon identifiers for world markers.
 * World rendering and waypoint persistence are not connected yet.
 */
public final class WorldWaypointIcons {
    private final List<Waypoint> waypoints = new ArrayList<Waypoint>();

    public void addWaypoint(String name, String iconId, double x, double y, double z) {
        if (name == null || name.trim().isEmpty()
                || iconId == null || iconId.trim().isEmpty()) {
            return;
        }
        waypoints.add(new Waypoint(name, iconId, x, y, z));
    }

    public boolean removeWaypoint(String name) {
        if (name == null) {
            return false;
        }
        for (int i = 0; i < waypoints.size(); i++) {
            if (name.equals(waypoints.get(i).getName())) {
                waypoints.remove(i);
                return true;
            }
        }
        return false;
    }

    public List<Waypoint> getWaypoints() {
        return Collections.unmodifiableList(new ArrayList<Waypoint>(waypoints));
    }

    public static final class Waypoint {
        private final String name;
        private final String iconId;
        private final double x;
        private final double y;
        private final double z;

        public Waypoint(String name, String iconId, double x, double y, double z) {
            this.name = name;
            this.iconId = iconId;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public String getName() { return name; }
        public String getIconId() { return iconId; }
        public double getX() { return x; }
        public double getY() { return y; }
        public double getZ() { return z; }
    }
}
