package com.alpharip.staffmod.caseescalationtimer;

/** Deadline calculations independent from Minecraft and wall-clock access. */
public final class EscalationDeadline {
    private EscalationDeadline() { }

    public static long createDeadline(long openedAt, long durationMillis) {
        if (openedAt < 0L || durationMillis <= 0L) throw new IllegalArgumentException("A positive deadline duration is required");
        return durationMillis > Long.MAX_VALUE - openedAt ? Long.MAX_VALUE : openedAt + durationMillis;
    }

    public static long remainingMillis(EscalationCase escalationCase, long now) {
        if (escalationCase == null || escalationCase.getDeadlineAt() == 0L) return -1L;
        if (escalationCase.getState() == EscalationCaseState.PAUSED)
            return escalationCase.getDeadlineAt() - escalationCase.getPausedAt();
        if (escalationCase.getState() == EscalationCaseState.RESOLVED) return 0L;
        return escalationCase.getDeadlineAt() - now;
    }
}
