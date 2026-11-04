package com.alpharip.staffmod.waypoints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores a multi-stop route and calculates both direct and segment distances. */
public final class RouteBetweenWaypoints {
    private final List<String> waypointIds = new ArrayList<String>();

    public boolean set(String startId, String endId) {
        if (blank(startId) || blank(endId)) return false;
        waypointIds.clear();
        waypointIds.add(startId.trim());
        waypointIds.add(endId.trim());
        return true;
    }

    public boolean setPath(List<StaffWaypoint> waypoints) {
        if (waypoints == null || waypoints.size() < 2) return false;
        String server=null;
        Integer dimension=null; String worldKey=null;
        List<String> ids=new ArrayList<String>();
        for(StaffWaypoint point:waypoints) {
            if(point==null||blank(point.getId()))return false;
            if(server==null){server=point.getServerKey();dimension=point.getDimensionId();worldKey=point.getWorldKey();}
            else if(!server.equals(point.getServerKey())||dimension.intValue()!=point.getDimensionId()||!worldKey.equals(point.getWorldKey()))return false;
            ids.add(point.getId());
        }
        waypointIds.clear();
        waypointIds.addAll(ids);
        return true;
    }

    public void clear() { waypointIds.clear(); }
    public boolean isActive() { return waypointIds.size()>=2; }
    public String getStartWaypointId() { return waypointIds.isEmpty()?"":waypointIds.get(0); }
    public String getEndWaypointId() { return waypointIds.size()<2?"":waypointIds.get(waypointIds.size()-1); }
    public List<String> getWaypointIds() { return Collections.unmodifiableList(new ArrayList<String>(waypointIds)); }
    public boolean contains(String id) { return waypointIds.contains(id); }

    /** Direct, endpoint-to-endpoint distance, retained for comparison with route distance. */
    public double getDistance(StaffWaypoint start, StaffWaypoint end) {
        return segmentDistance(start,end);
    }

    public double getTotalDistance(List<StaffWaypoint> points) {
        if(points==null||points.size()<2)return Double.NaN;
        double total=0.0D;
        for(int i=1;i<points.size();i++) {
            double segment=segmentDistance(points.get(i-1),points.get(i));
            if(Double.isNaN(segment))return Double.NaN;
            total+=segment;
        }
        return total;
    }

    public static double segmentDistance(StaffWaypoint start, StaffWaypoint end) {
        if (start == null || end == null || start.getDimensionId() != end.getDimensionId()
                || !start.getServerKey().equals(end.getServerKey()) || !start.getWorldKey().equals(end.getWorldKey())) return Double.NaN;
        double dx=end.getX()-start.getX(), dy=end.getY()-start.getY(), dz=end.getZ()-start.getZ();
        return Math.sqrt(dx*dx+dy*dy+dz*dz);
    }

    public static double bearing(double fromX, double fromZ, double toX, double toZ) {
        double degrees=Math.toDegrees(Math.atan2(toX-fromX, toZ-fromZ));
        return (degrees + 360.0D) % 360.0D;
    }
    private static boolean blank(String s) { return s == null || s.trim().isEmpty(); }
}
