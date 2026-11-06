package com.alpharip.staffmod.staffdashboardexpansion.overloadedstaffalert;

/** In-memory workload record for one staff member. */
public final class StaffWorkload {
    private final String staffId;
    private String staffName;
    private int openTasks;

    public StaffWorkload(String staffId, String staffName) {
        this.staffId = requireText(staffId, "staffId");
        this.staffName = requireText(staffName, "staffName");
        this.openTasks = 0;
    }

    private StaffWorkload(String staffId, String staffName, int openTasks) {
        this.staffId = staffId;
        this.staffName = staffName;
        this.openTasks = openTasks;
    }

    public String getStaffId() {
        return staffId;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = requireText(staffName, "staffName");
    }

    public int getOpenTasks() {
        return openTasks;
    }

    public void setOpenTasks(int openTasks) {
        if (openTasks < 0) {
            throw new IllegalArgumentException("openTasks cannot be negative.");
        }
        this.openTasks = openTasks;
    }

    public void addTask() {
        openTasks++;
    }

    public void completeTask() {
        if (openTasks == 0) {
            throw new IllegalStateException("There are no open tasks to complete.");
        }
        openTasks--;
    }

    public StaffWorkload copy() {
        return new StaffWorkload(staffId, staffName, openTasks);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
        return value.trim();
    }
}
