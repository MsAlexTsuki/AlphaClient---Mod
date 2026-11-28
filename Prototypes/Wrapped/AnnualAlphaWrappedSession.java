package com.alpharip.staffmod.wrapped;

/** Immutable local session sample used by the annual summary prototype. */
public final class AnnualAlphaWrappedSession {
    private final long startedAtMillis;
    private final long endedAtMillis;
    private final String serverId;

    public AnnualAlphaWrappedSession(long startedAtMillis, long endedAtMillis, String serverId) {
        if (startedAtMillis < 0L) throw new IllegalArgumentException("startedAtMillis must not be negative");
        if (endedAtMillis != 0L && endedAtMillis < startedAtMillis) {
            throw new IllegalArgumentException("endedAtMillis cannot be before startedAtMillis");
        }
        this.startedAtMillis = startedAtMillis;
        this.endedAtMillis = endedAtMillis;
        this.serverId = serverId == null ? "" : serverId.trim();
    }

    public long getStartedAtMillis() { return startedAtMillis; }
    /** Zero means the session is still open and should use the calculation reference time. */
    public long getEndedAtMillis() { return endedAtMillis; }
    public String getServerId() { return serverId; }
}
