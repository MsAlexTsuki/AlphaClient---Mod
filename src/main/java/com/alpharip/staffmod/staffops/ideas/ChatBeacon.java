package com.alpharip.staffmod.staffops.ideas;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/** Coordinate extraction and waypoint data model; chat click handling and map rendering are pending. */
public final class ChatBeacon {
    private static final Pattern COORDINATES = Pattern.compile("(?<!\\\\d)(-?\\\\d{1,7})\\\\s*[, ]\\\\s*(-?\\\\d{1,7})(?:\\\\s*[, ]\\\\s*(-?\\\\d{1,7}))?(?!\\\\d)");
    public static final class Waypoint {
        public final int x, y, z;
        public final boolean hasY;
        public final double distance;
        public Waypoint(int x, int y, int z, boolean hasY, double distance) {
            this.x=x; this.y=y; this.z=z; this.hasY=hasY; this.distance=distance;
        }
    }
    public Waypoint parseFirst(String message, double playerX, double playerY, double playerZ) {
        if (message == null) return null;
        Matcher matcher = COORDINATES.matcher(message);
        if (!matcher.find()) return null;
        try {
            int x = Integer.parseInt(matcher.group(1));
            boolean hasY = matcher.group(3) != null;
            int y = hasY ? Integer.parseInt(matcher.group(2)) : 0;
            int z = hasY ? Integer.parseInt(matcher.group(3)) : Integer.parseInt(matcher.group(2));
            double dx=x-playerX, dz=z-playerZ, dy=hasY ? y-playerY : 0D;
            return new Waypoint(x,y,z,hasY,Math.sqrt(dx*dx+dy*dy+dz*dz));
        } catch (NumberFormatException ignored) { return null; }
    }
}
