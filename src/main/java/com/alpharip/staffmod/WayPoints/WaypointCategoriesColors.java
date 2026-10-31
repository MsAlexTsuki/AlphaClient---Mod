package com.alpharip.staffmod.WayPoints;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Shared color palette for waypoint categories. */
public final class WaypointCategoriesColors {
    private static final Map<String, Integer> COLORS;
    static {
        Map<String, Integer> colors = new LinkedHashMap<String, Integer>();
        colors.put("General", 0xFFC83D);
        colors.put("Death", 0xFFFF5555);
        colors.put("Home", 0xFF55FF55);
        colors.put("Event", 0xFF55FFFF);
        colors.put("Resource", 0xFFFFAA00);
        colors.put("Friend", 0xFFAA55FF);
        COLORS = Collections.unmodifiableMap(colors);
    }
    private final Map<String, String> categories = new LinkedHashMap<String, String>();
    private final Map<String, Integer> overrides = new LinkedHashMap<String, Integer>();

    public void set(String waypoint, String category, int rgb) {
        if (waypoint == null || waypoint.trim().isEmpty()) throw new IllegalArgumentException("waypoint");
        if (category == null || category.trim().isEmpty()) throw new IllegalArgumentException("category");
        if (rgb < 0 || rgb > 0xFFFFFF) throw new IllegalArgumentException("rgb");
        categories.put(waypoint.trim(), category.trim());
        overrides.put(waypoint.trim(), rgb & 0xFFFFFF);
        StaffWaypoint point = WaypointManager.getInstance().find(waypoint.trim());
        if (point != null) {
            point.setCategory(category.trim());
            point.setColor(rgb);
            WaypointManager.getInstance().saveChanges();
        }
    }
    public String getCategory(String waypoint) {
        StaffWaypoint point = WaypointManager.getInstance().find(waypoint);
        return point == null ? categories.get(waypoint) : point.getCategory();
    }
    public Integer getColor(String waypoint) {
        StaffWaypoint point = WaypointManager.getInstance().find(waypoint);
        return point == null ? overrides.get(waypoint) : point.getColor();
    }
    public static int colorFor(String category) {
        Integer value = COLORS.get(category == null ? "General" : category);
        return value == null ? COLORS.get("General") : value;
    }
    public static Map<String, Integer> getPalette() { return COLORS; }
    public Map<String, String> getCategories() {
        return Collections.unmodifiableMap(new LinkedHashMap<String, String>(categories));
    }
}
