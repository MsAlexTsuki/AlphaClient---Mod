package com.alpharip.staffdashboardexpansion.availability;

/** One staff member's locally tracked availability. */
public final class StaffAvailabilityEntry {
    private final String staffName;
    private StaffAvailabilityStatus status;
    private String note;
    private long updatedAt;

    public StaffAvailabilityEntry(String staffName, StaffAvailabilityStatus status, String note) {
        if (staffName == null || staffName.trim().isEmpty()) throw new IllegalArgumentException("Staff name is required");
        if (status == null) throw new IllegalArgumentException("Status is required");
        this.staffName = staffName.trim();
        this.status = status;
        this.note = note == null ? "" : note.trim();
        this.updatedAt = System.currentTimeMillis();
    }
    public String getStaffName() { return staffName; }
    public StaffAvailabilityStatus getStatus() { return status; }
    public String getNote() { return note; }
    public long getUpdatedAt() { return updatedAt; }
    public void setStatus(StaffAvailabilityStatus status) {
        if (status == null) throw new IllegalArgumentException("Status is required");
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }
    public void setNote(String note) {
        this.note = note == null ? "" : note.trim();
        this.updatedAt = System.currentTimeMillis();
    }
}
