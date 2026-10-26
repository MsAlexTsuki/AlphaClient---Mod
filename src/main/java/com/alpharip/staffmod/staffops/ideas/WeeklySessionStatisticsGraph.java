package com.alpharip.staffmod.staffops.ideas;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Foundation for weekly session statistics data.
 * TODO(Supabase): load and persist historical session aggregates.
 * Data collection, graph rendering, date-boundary handling, and tests remain to be implemented.
 */
public class WeeklySessionStatisticsGraph {
    private final Map<LocalDate, Long> sessionMillisByDay = new LinkedHashMap<LocalDate, Long>();

    public void recordSession(LocalDate day, long durationMillis) {
        if (day == null || durationMillis <= 0L) return;
        Long current = sessionMillisByDay.get(day);
        sessionMillisByDay.put(day, (current == null ? 0L : current) + durationMillis);
    }

    public Map<LocalDate, Long> getSessionMillisByDay() {
        return Collections.unmodifiableMap(new LinkedHashMap<LocalDate, Long>(sessionMillisByDay));
    }
}