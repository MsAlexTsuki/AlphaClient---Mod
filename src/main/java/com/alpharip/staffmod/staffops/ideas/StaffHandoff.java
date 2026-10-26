package com.alpharip.staffmod.staffops.ideas;

/** Local handoff note model; it does not assign or notify another staff member. */
public final class StaffHandoff {
    private String fromStaff = "";
    private String toStaff = "";
    private String summary = "";
    private long createdAtMillis;

    public void prepare(String fromStaff, String toStaff, String summary, long createdAtMillis) {
        this.fromStaff = fromStaff == null ? "" : fromStaff;
        this.toStaff = toStaff == null ? "" : toStaff;
        this.summary = summary == null ? "" : summary;
        this.createdAtMillis = createdAtMillis;
    }

    public String getFromStaff() { return fromStaff; }
    public String getToStaff() { return toStaff; }
    public String getSummary() { return summary; }
    public long getCreatedAtMillis() { return createdAtMillis; }

    // TODO(Supabase): define authorized shared handoff storage and recipient notifications.
}