package com.alpharip.staffdashboardexpansion.competency;

/** A skill or responsibility that can be assessed for staff members. */
public final class StaffCompetency {
    private final String id;
    private final String name;
    private final String category;
    private final String description;
    private final long createdAt;

    StaffCompetency(String id, String name, String category, String description) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public long getCreatedAt() { return createdAt; }
}
