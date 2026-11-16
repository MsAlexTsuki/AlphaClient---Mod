package com.alpharip.staffmod.caseescalationtimer;

/** Caller-supplied warning window; no Alpha escalation SLA is assumed. */
public final class EscalationPolicy {
    private final long warningBeforeMillis;

    public EscalationPolicy(long warningBeforeMillis) {
        if (warningBeforeMillis < 0L) throw new IllegalArgumentException("Warning window cannot be negative");
        this.warningBeforeMillis = warningBeforeMillis;
    }

    public static EscalationPolicy withoutWarnings() { return new EscalationPolicy(0L); }
    public long getWarningBeforeMillis() { return warningBeforeMillis; }
}
