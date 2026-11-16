package com.alpharip.staffmod.blindcasereview;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** In-memory coordinator. The host must supply cases and enforce access rules. */
public final class BlindCaseReviewService {
    private final Map<String, BlindReviewSession> sessions =
            new LinkedHashMap<String, BlindReviewSession>();
    private final BlindCaseAnonymizer anonymizer = new BlindCaseAnonymizer();

    public synchronized BlindReviewSession openSession(String sessionId, BlindCase blindCase) {
        if (sessions.containsKey(sessionId)) {
            throw new IllegalArgumentException("A session with this ID already exists");
        }
        BlindReviewSession session = new BlindReviewSession(sessionId, blindCase, anonymizer);
        sessions.put(session.getSessionId(), session);
        return session;
    }

    public synchronized BlindReviewSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public synchronized Collection<BlindReviewSession> getSessions() {
        return Collections.unmodifiableList(
                new java.util.ArrayList<BlindReviewSession>(sessions.values()));
    }

    public synchronized boolean removeSession(String sessionId) {
        return sessions.remove(sessionId) != null;
    }
}
