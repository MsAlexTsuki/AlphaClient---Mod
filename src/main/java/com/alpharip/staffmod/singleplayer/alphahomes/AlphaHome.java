package com.alpharip.staffmod.singleplayer.alphahomes;

/** Immutable home entry stored inside its singleplayer world's save folder. */
public final class AlphaHome {
    private String name;
    private double x;
    private double y;
    private double z;
    private int dimension;
    private String waypointId;
    private String waypointServerKey;
    private String waypointWorldKey;

    public AlphaHome() { }

    public AlphaHome(String name, double x, double y, double z, int dimension,
                     String waypointId, String waypointServerKey, String waypointWorldKey) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
        this.waypointId = waypointId == null ? "" : waypointId;
        this.waypointServerKey = waypointServerKey == null ? "" : waypointServerKey;
        this.waypointWorldKey = waypointWorldKey == null ? "" : waypointWorldKey;
    }

    public String getName() { return name == null ? "" : name; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public int getDimension() { return dimension; }
    public String getWaypointId() { return waypointId == null ? "" : waypointId; }
    public String getWaypointServerKey() { return waypointServerKey == null ? "" : waypointServerKey; }
    public String getWaypointWorldKey() { return waypointWorldKey == null ? "" : waypointWorldKey; }

    public void update(double x, double y, double z, int dimension,
                       String waypointId, String waypointServerKey, String waypointWorldKey) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
        this.waypointId = waypointId == null ? "" : waypointId;
        this.waypointServerKey = waypointServerKey == null ? "" : waypointServerKey;
        this.waypointWorldKey = waypointWorldKey == null ? "" : waypointWorldKey;
    }

    public void normalize() {
        if (name == null) name = "";
        name = name.trim();
        if (waypointId == null) waypointId = "";
        if (waypointServerKey == null) waypointServerKey = "";
        if (waypointWorldKey == null) waypointWorldKey = "";
    }
}
