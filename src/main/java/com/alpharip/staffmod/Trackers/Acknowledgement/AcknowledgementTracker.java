package com.alpharip.staffmod.trackers.acknowledgement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main facade for the local acknowledgement workflow prototype.
 * Legacy string-record methods remain available for compatibility with the idea stub.
 */
public final class AcknowledgementTracker {
    public static final String FEATURE_NAME = "Acknowledgement Tracker";
    public static final boolean REQUIRES_SUPABASE = false;

    private final AcknowledgementService service;
    private final List<String> legacyRecords = new ArrayList<String>();

    public AcknowledgementTracker() {
        this(new AcknowledgementService());
    }

    public AcknowledgementTracker(AcknowledgementService service) {
        if (service == null) {
            throw new IllegalArgumentException("service must not be null.");
        }
        this.service = service;
    }

    public boolean isEnabled() {
        return service.getSettings().isEnabled();
    }

    public void setEnabled(boolean enabled) {
        service.getSettings().setEnabled(enabled);
    }

    public AcknowledgementService getService() {
        return service;
    }

    public AcknowledgementEntry addAcknowledgement(String createdBy, String subject,
                                                    String details) {
        return service.create(createdBy, subject, details);
    }

    public boolean acknowledge(long id, String staffName) {
        return service.acknowledge(id, staffName);
    }

    /** Compatibility method for the original plain-string idea foundation. */
    public void addRecord(String record) {
        if (record != null && !record.trim().isEmpty()) {
            legacyRecords.add(record);
            service.create("Local", record.trim(), "");
        }
    }

    /** Returns the original plain-string record view. */
    public List<String> getRecords() {
        return Collections.unmodifiableList(
            new ArrayList<String>(legacyRecords));
    }

    public void clearRecords() {
        legacyRecords.clear();
        service.clear();
    }
}
