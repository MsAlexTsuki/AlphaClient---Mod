package com.alpharip.staffmod.waypoints;

import java.util.UUID;

/** Persistent, local waypoint belonging to one server and dimension. */
public final class StaffWaypoint {
    private String id = UUID.randomUUID().toString();
    private String label = "";
    private String serverKey = "singleplayer";
    private String worldKey = "";
    private String dimension = "0";
    private int dimensionId;
    private double x;
    private double y;
    private double z;
    private String category = "General";
    private int color = 0xFFC83D;
    private String iconId = "pin";
    private String profileId = "default";
    private boolean deathWaypoint;
    private boolean favorite;
    private boolean visited;
    private boolean chromaEnabled;
    private long lastModified = System.currentTimeMillis();

    public StaffWaypoint() { }

    public StaffWaypoint(String label, String serverKey, int dimensionId, double x, double y, double z) {
        this.label = clean(label, "Waypoint");
        this.serverKey = clean(serverKey, "singleplayer");
        if (worldKey == null) worldKey = "";
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
    public String getWorldKey() { return worldKey == null ? "" : worldKey; }
    public String getDimension() { return dimension; }
    public int getDimensionId() { return dimensionId; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public String getCategory() { return category; }
    public int getColor() { return color; }
    public String getIconId() { return iconId; }
    public String getProfileId() { return profileId; }
    public boolean isDeathWaypoint() { return deathWaypoint; }
    public boolean isFavorite() { return favorite; }
    public boolean isVisited() { return visited; }
    public boolean isChromaEnabled() { return chromaEnabled; }
    public long getLastModified() { return lastModified; }
    private void touch() { lastModified=System.currentTimeMillis(); }

    /** Repairs optional fields when loading older or partially written local records. */
    void normalize() {
        if (id == null || id.trim().isEmpty()) id = UUID.randomUUID().toString();
        label = clean(label, "Waypoint");
        serverKey = clean(serverKey, "singleplayer");
        if (worldKey == null) worldKey = "";
        dimension = String.valueOf(dimensionId);
        category = clean(category, "General");
        iconId = clean(iconId, "pin");
        profileId = clean(profileId, "default");
        if (lastModified <= 0L) lastModified = System.currentTimeMillis();
        color = 0xFF000000 | (color & 0xFFFFFF);
    }

    public void set(String label, String dimension, double x, double y, double z) {
        this.label = clean(label, "Waypoint");
        this.dimension = clean(dimension, "0");
        try { this.dimensionId = Integer.parseInt(this.dimension); } catch (NumberFormatException ignored) { }
        this.x = x; this.y = y; this.z = z; touch();
    }
    public void setId(String value) { if (value != null && !value.trim().isEmpty()) id = value.trim(); }
    public void setLabel(String value) { label = clean(value, "Waypoint"); touch(); }
    public void setWorldKey(String value) { worldKey = value == null ? "" : value.trim(); touch(); }
    public void setContext(String server, int dimension) {
        serverKey = clean(server, "singleplayer");
        dimensionId = dimension;
        this.dimension = String.valueOf(dimension); touch();
    }
    public void setCategory(String value) { category = clean(value, "General"); touch(); }
    public void setColor(int value) { color = 0xFF000000 | (value & 0xFFFFFF); touch(); }
    public void setIconId(String value) { iconId = clean(value, "pin"); touch(); }
    public void setProfileId(String value) { profileId = clean(value, "default"); touch(); }
    public void setDeathWaypoint(boolean value) { deathWaypoint = value; touch(); }
    public void setFavorite(boolean value) { if(favorite!=value){favorite=value;touch();} }
    public void setVisited(boolean value) { if(visited!=value){visited=value;touch();} }
    public void setChromaEnabled(boolean value) { if(chromaEnabled!=value){chromaEnabled=value;touch();} }
}
