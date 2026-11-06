package com.alpharip.staffmod.staffdashboard.responsibility;

/** A single responsibility tracked by the staff board. */
public class StaffResponsibility {
    private final int id;
    private final long createdAt;
    private String title;
    private String description;
    private String assignee;
    private StaffResponsibilityPriority priority;
    private StaffResponsibilityStatus status;

    public StaffResponsibility(int id, String title, String description, String assignee,
                               StaffResponsibilityPriority priority) {
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Title is required.");
        if (description == null || description.trim().isEmpty()) throw new IllegalArgumentException("Description is required.");
        if (priority == null) throw new IllegalArgumentException("Priority is required.");
        this.id = id;
        this.createdAt = System.currentTimeMillis();
        this.title = title.trim();
        this.description = description.trim();
        this.assignee = assignee == null ? "" : assignee.trim();
        this.priority = priority;
        this.status = this.assignee.isEmpty() ? StaffResponsibilityStatus.UNASSIGNED : StaffResponsibilityStatus.ASSIGNED;
    }

    public int getId() { return id; }
    public long getCreatedAt() { return createdAt; }
    public String getTitle() { return title; }
    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Title is required.");
        this.title = title.trim();
    }
    public String getDescription() { return description; }
    public void setDescription(String description) {
        if (description == null || description.trim().isEmpty()) throw new IllegalArgumentException("Description is required.");
        this.description = description.trim();
    }
    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) {
        this.assignee = assignee == null ? "" : assignee.trim();
        if (this.assignee.isEmpty() && status == StaffResponsibilityStatus.ASSIGNED) status = StaffResponsibilityStatus.UNASSIGNED;
        else if (!this.assignee.isEmpty() && status == StaffResponsibilityStatus.UNASSIGNED) status = StaffResponsibilityStatus.ASSIGNED;
    }
    public StaffResponsibilityPriority getPriority() { return priority; }
    public void setPriority(StaffResponsibilityPriority priority) {
        if (priority == null) throw new IllegalArgumentException("Priority is required.");
        this.priority = priority;
    }
    public StaffResponsibilityStatus getStatus() { return status; }
    public void setStatus(StaffResponsibilityStatus status) {
        if (status == null) throw new IllegalArgumentException("Status is required.");
        if (status == StaffResponsibilityStatus.ASSIGNED && assignee.isEmpty()) {
            throw new IllegalStateException("Assign a staff member before setting this status.");
        }
        this.status = status;
    }
}
