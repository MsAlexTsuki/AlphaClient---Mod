package com.alpharip.staffmod.wrapped;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Calculates activity overlapping a calendar year using supplied local session samples. */
public final class AnnualAlphaWrappedCalculator {
    private AnnualAlphaWrappedCalculator() { }

    public static AnnualAlphaWrappedSummary calculate(int year, List<AnnualAlphaWrappedSession> sessions,
                                                       ZoneId zone, long referenceTimeMillis) {
        if (zone == null) throw new IllegalArgumentException("zone is required");
        if (sessions == null) throw new IllegalArgumentException("sessions are required");

        long yearStart = LocalDate.of(year, 1, 1).atStartOfDay(zone).toInstant().toEpochMilli();
        long yearEnd = LocalDate.of(year + 1, 1, 1).atStartOfDay(zone).toInstant().toEpochMilli();
        long totalMillis = 0L;
        long sessionCount = 0L;
        Set<String> servers = new HashSet<String>();

        for (AnnualAlphaWrappedSession session : sessions) {
            if (session == null) continue;
            long start = session.getStartedAtMillis();
            long end = session.getEndedAtMillis() == 0L
                    ? Math.max(start, referenceTimeMillis) : session.getEndedAtMillis();
            long overlapStart = Math.max(start, yearStart);
            long overlapEnd = Math.min(end, yearEnd);
            if (overlapEnd <= overlapStart) continue;

            totalMillis += overlapEnd - overlapStart;
            sessionCount++;
            if (!session.getServerId().isEmpty()) servers.add(session.getServerId());
        }

        boolean available = sessionCount > 0L;
        return new AnnualAlphaWrappedSummary(year, totalMillis, sessionCount, servers.size(), available);
    }

    public static AnnualAlphaWrappedSummary empty(int year) {
        return new AnnualAlphaWrappedSummary(year, 0L, 0L, 0L, false);
    }
}
