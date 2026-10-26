package com.alpharip.staffmod.staffops.ideas;

/** Basic summary metadata for a locally tracked investigation session. */
public final class InvestigationSessionSummary {
    private String sessionId;
    private long startedAtMillis;
    private long endedAtMillis;
    private int eventCount;
    private int noteCount;
    private String summary = "";

    public synchronized void setSessionId(String value) { sessionId = value; }
    public synchronized String getSessionId() { return sessionId; }
    public synchronized void setStartedAtMillis(long value) { startedAtMillis = value; }
    public synchronized long getStartedAtMillis() { return startedAtMillis; }
    public synchronized void setEndedAtMillis(long value) { endedAtMillis = value; }
    public synchronized long getEndedAtMillis() { return endedAtMillis; }
    public synchronized void setEventCount(int value) { eventCount = Math.max(0, value); }
    public synchronized int getEventCount() { return eventCount; }
    public synchronized void setNoteCount(int value) { noteCount = Math.max(0, value); }
    public synchronized int getNoteCount() { return noteCount; }
    public synchronized void setSummary(String value) { summary = value == null ? "" : value; }
    public synchronized String getSummary() { return summary; }
    public synchronized long getDurationMillis() {
        return endedAtMillis >= startedAtMillis ? endedAtMillis - startedAtMillis : 0L;
    }
}