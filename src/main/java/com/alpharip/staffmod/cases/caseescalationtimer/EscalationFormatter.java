package com.alpharip.staffmod.caseescalationtimer;

import java.util.concurrent.TimeUnit;

public final class EscalationFormatter {
    private EscalationFormatter() { }

    public static EscalationStatus status(EscalationCase item, EscalationPolicy policy, long now) {
        if (item == null) return EscalationStatus.NO_DEADLINE;
        if (item.getState() == EscalationCaseState.RESOLVED) return EscalationStatus.RESOLVED;
        if (item.getState() == EscalationCaseState.PAUSED) return EscalationStatus.PAUSED;
        if (item.getDeadlineAt() == 0L) return EscalationStatus.NO_DEADLINE;
        long remaining = item.getDeadlineAt() - now;
        if (remaining < 0L) return EscalationStatus.OVERDUE;
        if (remaining == 0L) return EscalationStatus.DUE;
        if (policy != null && policy.getWarningBeforeMillis() > 0L
                && remaining <= policy.getWarningBeforeMillis()) return EscalationStatus.WARNING;
        return EscalationStatus.OPEN;
    }

    public static String remaining(long millis) {
        if (millis < 0L) return "Overdue by " + duration(-millis);
        if (millis == 0L) return "Due now";
        return duration(millis) + " remaining";
    }

    public static String duration(long millis) {
        long safe = Math.max(0L, millis);
        long days = TimeUnit.MILLISECONDS.toDays(safe);
        long hours = TimeUnit.MILLISECONDS.toHours(safe) % 24L;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(safe) % 60L;
        if (days > 0L) return days + "d " + hours + "h";
        if (hours > 0L) return hours + "h " + minutes + "m";
        return minutes + "m";
    }
}
