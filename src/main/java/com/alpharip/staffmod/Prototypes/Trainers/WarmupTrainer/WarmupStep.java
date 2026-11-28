package com.alpharip.staffmod.Trainers.WarmupTrainer;

/** One timed exercise in a warm-up routine. Java 8 compatible. */
public final class WarmupStep {
    private final String id;
    private final String title;
    private final String description;
    private final int durationSeconds;

    public WarmupStep(String id, String title, String description, int durationSeconds) {
        this.id = requireText(id, "id");
        this.title = requireText(title, "title");
        this.description = description == null ? "" : description.trim();
        if (durationSeconds < 1) throw new IllegalArgumentException("durationSeconds must be positive.");
        this.durationSeconds = durationSeconds;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getDurationSeconds() { return durationSeconds; }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(name + " must not be blank.");
        return value.trim();
    }
}
