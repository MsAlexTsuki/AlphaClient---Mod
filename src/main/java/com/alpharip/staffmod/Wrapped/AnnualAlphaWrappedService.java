package com.alpharip.staffmod.wrapped;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

/**
 * Prototype service boundary. The caller supplies session samples; this class
 * does not access Supabase, account APIs, or AlphaClient Core.
 */
public final class AnnualAlphaWrappedService {
    private AnnualAlphaWrappedService() { }

    public static AnnualAlphaWrappedSummary createSummary(int year,
            List<AnnualAlphaWrappedSession> localSessions, ZoneId zone, long referenceTimeMillis,
            AnnualAlphaWrappedSettings settings) {
        if (settings == null || !settings.isEnabled()) return AnnualAlphaWrappedCalculator.empty(year);
        List<AnnualAlphaWrappedSession> safeSessions = localSessions == null
                ? Collections.<AnnualAlphaWrappedSession>emptyList() : localSessions;
        return AnnualAlphaWrappedCalculator.calculate(year, safeSessions, zone, referenceTimeMillis);
    }
}
