package com.alpharip.staffmod.staffops.ideas;

/** Local coordinate marker for evidence context already known to the client. */
public final class EvidenceLocationMarker {
    private String label = "";
    private String dimension = "";
    private double x, y, z;
    private boolean active;

    public void set(String label, String dimension, double x, double y, double z) {
        this.label = label == null ? "" : label;
        this.dimension = dimension == null ? "" : dimension;
        this.x = x; this.y = y; this.z = z;
        active = true;
    }

    public void clear() {
        label = ""; dimension = "";
        x = y = z = 0.0D;
        active = false;
    }

    public String getLabel() { return label; }
    public String getDimension() { return dimension; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public boolean isActive() { return active; }
}