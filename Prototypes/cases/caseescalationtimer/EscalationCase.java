package com.alpharip.staffmod.caseescalationtimer;

/** Immutable case timer record. Times are Unix epoch milliseconds. */
public final class EscalationCase {
    private final String id;
    private final String title;
    private final String reference;
    private final long openedAt;
    private final long deadlineAt;
    private final EscalationCaseState state;
    private final long pausedAt;
    private final long totalPausedMillis;

    public EscalationCase(String id, String title, String reference, long openedAt,
                          long deadlineAt, EscalationCaseState state,
                          long pausedAt, long totalPausedMillis) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("Case ID is required");
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Case title is required");
        if (openedAt < 0L || deadlineAt < 0L || totalPausedMillis < 0L || pausedAt < 0L)
            throw new IllegalArgumentException("Timestamps cannot be negative");
        if (deadlineAt > 0L && deadlineAt < openedAt) throw new IllegalArgumentException("Deadline precedes opening time");
        this.id = id.trim();
        this.title = title.trim();
        this.reference = reference == null ? "" : reference.trim();
        this.openedAt = openedAt;
        this.deadlineAt = deadlineAt;
        this.state = state == null ? EscalationCaseState.OPEN : state;
        this.pausedAt = pausedAt;
        this.totalPausedMillis = totalPausedMillis;
        if (this.state == EscalationCaseState.PAUSED && pausedAt == 0L)
            throw new IllegalArgumentException("Paused cases require a pause timestamp");
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getReference() { return reference; }
    public long getOpenedAt() { return openedAt; }
    public long getDeadlineAt() { return deadlineAt; }
    public EscalationCaseState getState() { return state; }
    public long getPausedAt() { return pausedAt; }
    public long getTotalPausedMillis() { return totalPausedMillis; }

    public EscalationCase pause(long now) {
        if (state != EscalationCaseState.OPEN) throw new IllegalStateException("Only open cases can be paused");
        return new EscalationCase(id, title, reference, openedAt, deadlineAt, EscalationCaseState.PAUSED, now, totalPausedMillis);
    }

    public EscalationCase resume(long now) {
        if (state != EscalationCaseState.PAUSED) throw new IllegalStateException("Only paused cases can be resumed");
        long paused = Math.max(0L, now - pausedAt);
        return new EscalationCase(id, title, reference, openedAt,
                deadlineAt == 0L ? 0L : safeAdd(deadlineAt, paused),
                EscalationCaseState.OPEN, 0L, safeAdd(totalPausedMillis, paused));
    }

    public EscalationCase resolve() {
        if (state == EscalationCaseState.RESOLVED) return this;
        return new EscalationCase(id, title, reference, openedAt, deadlineAt,
                EscalationCaseState.RESOLVED, 0L, totalPausedMillis);
    }

    private static long safeAdd(long a, long b) {
        return b > 0L && a > Long.MAX_VALUE - b ? Long.MAX_VALUE : a + b;
    }
}
