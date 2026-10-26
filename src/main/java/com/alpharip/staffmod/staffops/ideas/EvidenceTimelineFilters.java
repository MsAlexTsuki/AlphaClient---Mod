package com.alpharip.staffmod.staffops.ideas;


import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/** Local filter criteria for evidence timeline event types. */
public final class EvidenceTimelineFilters {
    private final Set<String> eventTypes = new LinkedHashSet<String>();
    private boolean includeUncategorized = true;

    public void addEventType(String eventType) {
        if (eventType != null && !eventType.trim().isEmpty()) {
            eventTypes.add(eventType.trim());
        }
    }

    public void removeEventType(String eventType) {
        if (eventType != null) eventTypes.remove(eventType.trim());
    }

    public void clearEventTypes() { eventTypes.clear(); }
    public Set<String> getEventTypes() {
        return Collections.unmodifiableSet(new LinkedHashSet<String>(eventTypes));
    }
    public boolean isIncludeUncategorized() { return includeUncategorized; }
    public void setIncludeUncategorized(boolean include) { includeUncategorized = include; }

    public boolean accepts(String eventType) {
        if (eventType == null || eventType.trim().isEmpty()) return includeUncategorized;
        return eventTypes.isEmpty() || eventTypes.contains(eventType.trim());
    }
}