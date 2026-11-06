package com.alpharip.staffdashboardexpansion.goals;

/** Mutable progress record for one staff-team goal. */
public final class StaffGoal {
    private final String id;
    private final String team;
    private final String title;
    private final String description;
    private final long createdAt;
    private final long dueAt;
    private StaffGoalStatus status;
    private int progress;
    private long updatedAt;

    StaffGoal(String id, String team, String title, String description,
              long dueAt, long createdAt) {
        this.id = id;
        this.team = team;
        this.title = title;
        this.description = description;
        this.dueAt = dueAt;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.status = StaffGoalStatus.OPEN;
        this.progress = 0;
    }

    public String getId() { return id; }
    public String getTeam() { return team; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public long getCreatedAt() { return createdAt; }
    public long getDueAt() { return dueAt; }
    public StaffGoalStatus getStatus() { return status; }
    public int getProgress() { return progress; }
    public long getUpdatedAt() { return updatedAt; }

    public boolean hasDeadline() {
        return dueAt > 0L;
    }

    public boolean isOverdue(long now) {
        return hasDeadline() && dueAt < now
            && status != StaffGoalStatus.COMPLETED
            && status != StaffGoalStatus.CANCELLED;
    }

    void setStatus(StaffGoalStatus status, long now) {
        this.status = status;
        if (status == StaffGoalStatus.COMPLETED) {
            this.progress = 100;
        } else if (status == StaffGoalStatus.OPEN && this.progress == 100) {
            this.progress = 0;
        }
        this.updatedAt = now;
    }

    void setProgress(int progress, long now) {
        this.progress = progress;
        if (progress == 100) {
            this.status = StaffGoalStatus.COMPLETED;
        } else if (progress > 0 && this.status == StaffGoalStatus.OPEN) {
            this.status = StaffGoalStatus.IN_PROGRESS;
        } else if (progress == 0 && this.status == StaffGoalStatus.IN_PROGRESS) {
            this.status = StaffGoalStatus.OPEN;
        }
        this.updatedAt = now;
    }
}
