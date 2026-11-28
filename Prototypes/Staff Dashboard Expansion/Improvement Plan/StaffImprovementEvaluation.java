package com.alpharip.staffmod.staffdashboard.improvement;

public class StaffImprovementEvaluation {
    private final long createdAt;
    private final String reviewer;
    private final String notes;
    private final int progressPercent;

    public StaffImprovementEvaluation(String reviewer, String notes, int progressPercent) {
        if (reviewer == null || reviewer.trim().isEmpty()) {
            throw new IllegalArgumentException("Reviewer cannot be empty.");
        }
        if (notes == null || notes.trim().isEmpty()) {
            throw new IllegalArgumentException("Evaluation notes cannot be empty.");
        }
        if (progressPercent < 0 || progressPercent > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100.");
        }
        this.createdAt = System.currentTimeMillis();
        this.reviewer = reviewer.trim();
        this.notes = notes.trim();
        this.progressPercent = progressPercent;
    }

    public long getCreatedAt() { return createdAt; }
    public String getReviewer() { return reviewer; }
    public String getNotes() { return notes; }
    public int getProgressPercent() { return progressPercent; }
}
