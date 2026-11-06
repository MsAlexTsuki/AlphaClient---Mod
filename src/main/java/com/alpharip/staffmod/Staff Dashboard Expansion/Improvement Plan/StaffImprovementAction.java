package com.alpharip.staffmod.staffdashboard.improvement;

/** One measurable action belonging to a staff improvement plan. */
public class StaffImprovementAction {
    private final int id;
    private String description;
    private boolean completed;

    public StaffImprovementAction(int id, String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Action description cannot be empty.");
        }
        this.id = id;
        this.description = description.trim();
    }

    public int getId() { return id; }
    public String getDescription() { return description; }
    public void setDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Action description cannot be empty.");
        }
        this.description = description.trim();
    }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
