package com.alpharip.staffmod.staffdashboardexpansion.staffhandoff;

/** A local staff-to-staff handoff record. */
public final class StaffHandoff {
    private final long id;
    private final String fromStaff;
    private final String toStaff;
    private final String summary;
    private final long createdAtMillis;
    private StaffHandoffStatus status;

    StaffHandoff(long id, String fromStaff, String toStaff, String summary,
                 long createdAtMillis) {
        this.id = id;
        this.fromStaff = fromStaff;
        this.toStaff = toStaff;
        this.summary = summary;
        this.createdAtMillis = createdAtMillis;
        this.status = StaffHandoffStatus.PENDING;
    }

    public long getId() { return id; }
    public String getFromStaff() { return fromStaff; }
    public String getToStaff() { return toStaff; }
    public String getSummary() { return summary; }
    public long getCreatedAtMillis() { return createdAtMillis; }
    public StaffHandoffStatus getStatus() { return status; }

    void setStatus(StaffHandoffStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null.");
        }
        this.status = status;
    }

    // TODO(Supabase): define authorized shared handoff storage and recipient notifications.
}
