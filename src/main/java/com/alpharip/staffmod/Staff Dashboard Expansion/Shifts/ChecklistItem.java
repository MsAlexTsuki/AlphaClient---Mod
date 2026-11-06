package com.alpharip.staffdashboardexpansion.shifts;

/** One editable checklist entry. */
public final class ChecklistItem {
    private final String id;
    private String title;
    private String description;
    private ChecklistCategory category;
    private boolean completed;

    public ChecklistItem(String id, String title, String description,
                         ChecklistCategory category) {
        this.id = requireText(id, "id");
        this.title = requireText(title, "title");
        this.description = description == null ? "" : description.trim();
        if (category == null) {
            throw new IllegalArgumentException("category must not be null.");
        }
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public ChecklistCategory getCategory() {
        return category;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void update(String title, String description, ChecklistCategory category) {
        this.title = requireText(title, "title");
        this.description = description == null ? "" : description.trim();
        if (category == null) {
            throw new IllegalArgumentException("category must not be null.");
        }
        this.category = category;
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
        return value.trim();
    }
}
