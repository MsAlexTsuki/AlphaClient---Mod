package com.alpharip.staffmod.caseescalationtimer;

/** Local notification event; it does not send chat or server commands. */
public final class EscalationNotification {
    private final String caseId;
    private final String caseTitle;
    private final EscalationNotificationType type;
    private final long createdAt;

    public EscalationNotification(String caseId, String caseTitle,
                                  EscalationNotificationType type, long createdAt) {
        this.caseId = caseId;
        this.caseTitle = caseTitle;
        this.type = type;
        this.createdAt = createdAt;
    }
    public String getCaseId() { return caseId; }
    public String getCaseTitle() { return caseTitle; }
    public EscalationNotificationType getType() { return type; }
    public long getCreatedAt() { return createdAt; }
}
