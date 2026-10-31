package com.alpharip.staffmod.WayPoints;

import java.util.UUID;

/** Persistent, local waypoint belonging to one server and dimension. */
public final class StaffWaypoint {
    private String id = UUID.randomUUID().toString();
    private String label = "";
    private String serverKey = "singleplayer";
    private String dimension = "0";
    private int dimensionId;
    private double x;
    private double y;
    private double z;
    private String category = "General";
    private int color = 0xFFC83D;
    private String iconId = "pin";
    private boolean deathWaypoint;

    public StaffWaypoint() { }

    public StaffWaypoint(String label, String serverKey, int dimensionId, double x, double y, double z) {
        this.label = clean(label, "Waypoint");
        this.serverKey = clean(serverKey, "singleplayer");
        this.dimensionId = dimensionId;
        this.dimension = String.valueOf(dimensionId);
        this.x = x; this.y = y; this.z = z;
    }

    private static String clean(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    public String getId() { return id; }
    public String getLabel() { return label; }
    public String getName() { return label; }
    public String getServerKey() { return serverKey; }
    public String getDimension() { return dimension; }
    public int getDimensionId() { return dimensionId; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public String getCategory() { return category; }
    public int getColor() { return color; }
    public String getIconId() { return iconId; }
    public boolean isDeathWaypoint() { return deathWaypoint; }

    /** Repairs optional fields when loading older or partially written local records. */
    void normalize() {
        if (id == null || id.trim().isEmpty()) id = UUID.randomUUID().toString();
        label = clean(label, "Waypoint");
        serverKey = clean(serverKey, "singleplayer");
        dimension = String.valueOf(dimensionId);
        category = clean(category, "General");
        iconId = clean(iconId, "pin");
        color = 0xFF000000 | (color & 0xFFFFFF);
    }

    public void set(String label, String dimension, double x, double y, double z) {
        this.label = clean(label, "Waypoint");
        this.dimension = clean(dimension, "0");
        try { this.dimensionId = Integer.parseInt(this.dimension); } catch (NumberFormatException ignored) { }
        this.x = x; this.y = y; this.z = z;
    }
    public void setId(String value) { if (value != null && !value.trim().isEmpty()) id = value.trim(); }
    public void setLabel(String value) { label = clean(value, "Waypoint"); }
    public void setContext(String server, int dimension) {
        serverKey = clean(server, "singleplayer");
        dimensionId = dimension;
        this.dimension = String.valueOf(dimension);
    }
    public void setCategory(String value) { category = clean(value, "General"); }
    public void setColor(int value) { color = 0xFF000000 | (value & 0xFFFFFF); }
    public void setIconId(String value) { iconId = clean(value, "pin"); }
    public void setDeathWaypoint(boolean value) { deathWaypoint = value; }
}
