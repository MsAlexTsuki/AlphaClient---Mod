package com.alpharip.staffmod.staffdashboardexpansion.staffqueue;

/** One local staff task in the queue. */
public final class StaffQueueEntry {
    private final long id;
    private final String title;
    private final String description;
    private final String createdBy;
    private final long createdAt;
    private final StaffQueuePriority priority;
    private StaffQueueStatus status;
    private String assignedTo;
    private long updatedAt;

    StaffQueueEntry(long id, String title, String description, String createdBy,
                    StaffQueuePriority priority, long createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
        this.priority = priority;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.status = StaffQueueStatus.OPEN;
        this.assignedTo = "";
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCreatedBy() { return createdBy; }
    public long getCreatedAt() { return createdAt; }
    public StaffQueuePriority getPriority() { return priority; }
    public StaffQueueStatus getStatus() { return status; }
    public String getAssignedTo() { return assignedTo; }
    public long getUpdatedAt() { return updatedAt; }

    void assignTo(String staffName, long now) {
        assignedTo = staffName;
        updatedAt = now;
    }

    void setStatus(StaffQueueStatus status, long now) {
        this.status = status;
        updatedAt = now;
    }
}
