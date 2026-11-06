package com.alpharip.staffmod.trackers.acknowledgement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Coordinates local acknowledgement creation, filtering, and acknowledgement. */
public final class AcknowledgementService {
    private final AcknowledgementSettings settings;
    private final AcknowledgementRegistry registry;

    public AcknowledgementService() {
        this(new AcknowledgementSettings());
    }

    public AcknowledgementService(AcknowledgementSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("settings must not be null.");
        }
        this.settings = settings;
        this.registry = new AcknowledgementRegistry(settings.getMaximumEntries());
    }

    public AcknowledgementSettings getSettings() { return settings; }
    public AcknowledgementRegistry getRegistry() { return registry; }

    public AcknowledgementEntry create(String createdBy, String subject, String details) {
        if (!settings.isEnabled()) {
            return null;
        }
        return registry.create(createdBy, subject, details, System.currentTimeMillis());
    }

    public boolean acknowledge(long id, String staffName) {
        if (!settings.isEnabled() || staffName == null || staffName.trim().isEmpty()) {
            return false;
        }
        return registry.acknowledge(id, staffName, System.currentTimeMillis());
    }

    public List<AcknowledgementEntry> getVisibleEntries(String query) {
        if (!settings.isEnabled()) {
            return Collections.emptyList();
        }
        List<AcknowledgementEntry> source = registry.search(query);
        if (settings.isShowAcknowledged()) {
            return source;
        }
        List<AcknowledgementEntry> result = new ArrayList<AcknowledgementEntry>();
        for (AcknowledgementEntry entry : source) {
            if (entry.getStatus() == AcknowledgementStatus.PENDING) {
                result.add(entry);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public void clear() {
        registry.clear();
    }
}
