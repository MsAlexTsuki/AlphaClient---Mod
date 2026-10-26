package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores observations associated with one session. */
public class SessionBehaviorProfile {
    private final String sessionId;
    private final List<String> observations = new ArrayList<String>();
    public SessionBehaviorProfile(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("sessionId cannot be blank");
        }
        this.sessionId = sessionId.trim();
    }
    public String getSessionId() { return sessionId; }
    public void addObservation(String value) {
        if (value != null && !value.trim().isEmpty()) { observations.add(value.trim()); }
    }
    public List<String> getObservations() {
        return Collections.unmodifiableList(new ArrayList<String>(observations));
    }
}