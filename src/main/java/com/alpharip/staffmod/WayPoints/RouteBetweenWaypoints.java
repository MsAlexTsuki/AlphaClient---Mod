package com.alpharip.staffmod.WayPoints;

/** Stores and calculates a straight-line route between two saved waypoint IDs. */
public final class RouteBetweenWaypoints {
    private String startWaypointId = "";
    private String endWaypointId = "";

    public boolean set(String startId, String endId) {
        if (blank(startId) || blank(endId)) return false;
        startWaypointId = startId.trim();
        endWaypointId = endId.trim();
        return true;
    }
    public void clear() { startWaypointId = ""; endWaypointId = ""; }
    public boolean isActive() { return !startWaypointId.isEmpty() && !endWaypointId.isEmpty(); }
    public String getStartWaypointId() { return startWaypointId; }
    public String getEndWaypointId() { return endWaypointId; }
    public double getDistance(StaffWaypoint start, StaffWaypoint end) {
        if (start == null || end == null || start.getDimensionId() != end.getDimensionId())
            return Double.NaN;
        double dx=end.getX()-start.getX(), dy=end.getY()-start.getY(), dz=end.getZ()-start.getZ();
        return Math.sqrt(dx*dx+dy*dy+dz*dz);
    }
    public static double bearing(double fromX, double fromZ, double toX, double toZ) {
        double degrees=Math.toDegrees(Math.atan2(toX-fromX, toZ-fromZ));
        return (degrees + 360.0D) % 360.0D;
    }
    private static boolean blank(String s) { return s == null || s.trim().isEmpty(); }
}
