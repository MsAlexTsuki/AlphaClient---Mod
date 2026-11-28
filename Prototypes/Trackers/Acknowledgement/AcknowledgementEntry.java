package com.alpharip.staffmod.trackers.acknowledgement;

/** One immutable-identity acknowledgement record with a controlled status transition. */
public final class AcknowledgementEntry {
    private final long id;
    private final String createdBy;
    private final String subject;
    private final String details;
    private final long createdAtMillis;
    private AcknowledgementStatus status;
    private String acknowledgedBy;
    private long acknowledgedAtMillis;

    AcknowledgementEntry(long id, String createdBy, String subject, String details,
                         long createdAtMillis) {
        if (id < 1L || createdAtMillis < 0L) {
            throw new IllegalArgumentException("Invalid acknowledgement identity or timestamp.");
        }
        this.id = id;
        this.createdBy = requireText(createdBy, "createdBy");
        this.subject = requireText(subject, "subject");
        this.details = details == null ? "" : details.trim();
        this.createdAtMillis = createdAtMillis;
        this.status = AcknowledgementStatus.PENDING;
        this.acknowledgedBy = "";
        this.acknowledgedAtMillis = 0L;
    }

    public long getId() { return id; }
    public String getCreatedBy() { return createdBy; }
    public String getSubject() { return subject; }
    public String getDetails() { return details; }
    public long getCreatedAtMillis() { return createdAtMillis; }
    public AcknowledgementStatus getStatus() { return status; }
    public String getAcknowledgedBy() { return acknowledgedBy; }
    public long getAcknowledgedAtMillis() { return acknowledgedAtMillis; }

    boolean acknowledge(String staffName, long timestamp) {
        if (status != AcknowledgementStatus.PENDING || timestamp < createdAtMillis) {
            return false;
        }
        acknowledgedBy = requireText(staffName, "staffName");
        acknowledgedAtMillis = timestamp;
        status = AcknowledgementStatus.ACKNOWLEDGED;
        return true;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank.");
        }
        return value.trim();
    }
}
