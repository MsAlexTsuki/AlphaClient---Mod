package com.alpharip.staffdashboardexpansion.coverage;

import java.time.LocalDateTime;

/** Minimum staffing target for a named time window. */
public final class CoverageRequirement {
    private final String id, name;
    private final LocalDateTime start, end;
    private final int minimumStaff;

    public CoverageRequirement(String id, String name, LocalDateTime start,
                               LocalDateTime end, int minimumStaff) {
        this.id = require(id, "id");
        this.name = require(name, "name");
        if (start == null || end == null || !end.isAfter(start))
            throw new IllegalArgumentException("End time must be after start time.");
        if (minimumStaff < 0) throw new IllegalArgumentException("Minimum staff cannot be negative.");
        this.start = start; this.end = end; this.minimumStaff = minimumStaff;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd() { return end; }
    public int getMinimumStaff() { return minimumStaff; }
    private static String require(String value, String field) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(field + " cannot be blank.");
        return value.trim();
    }
}
