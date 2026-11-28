package com.alpharip.staffmod.staffops.ideas;

import java.time.Instant;

/**
 * Foundation for explicitly opt-in performance telemetry.
 * TODO(Supabase): store consent and any approved, minimized telemetry aggregates.
 * Collection, consent UI, retention/deletion controls, and tests remain to be implemented.
 */
public class OptInPerformanceTelemetry {
    private boolean optedIn;
    private Instant consentUpdatedAt;

    public boolean isOptedIn() { return optedIn; }
    public Instant getConsentUpdatedAt() { return consentUpdatedAt; }

    public void setOptedIn(boolean value, Instant updatedAt) {
        optedIn = value;
        consentUpdatedAt = updatedAt;
    }
}