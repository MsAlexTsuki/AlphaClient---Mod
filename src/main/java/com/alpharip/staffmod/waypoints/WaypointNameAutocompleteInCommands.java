package com.alpharip.staffmod.waypoints;

import java.util.ArrayList;
import java.util.List;

/** Provides case-insensitive name completions for waypoint commands. */
public final class WaypointNameAutocompleteInCommands {
    public List<String> complete(String query, List<StaffWaypoint> waypoints) {
        String q = query == null ? "" : query.toLowerCase(java.util.Locale.ROOT);
        List<String> result = new ArrayList<String>();
        if (waypoints != null) for (StaffWaypoint point : waypoints) {
            if (point != null && point.getLabel().toLowerCase(java.util.Locale.ROOT).startsWith(q))
                result.add(point.getLabel());
        }
        return result;
    }
}
