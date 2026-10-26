package com.alpharip.staffmod.staffops.ideas;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Compares scheduled shifts with minimum staffing requirements for time windows.
 *
 * Forecasts use only the shifts supplied by the caller; they do not represent
 * live server coverage.
 * TODO(Supabase): load shared shifts and coverage requirements from authorized
 * records before exposing forecasts to staff.
 * Compatible with Java 8.
 */
public class StaffCoverageForecast {
    public static final class Shift {
        private final String staffId;
        private final LocalDateTime start;
        private final LocalDateTime end;

        public Shift(String staffId, LocalDateTime start, LocalDateTime end) {
            this.staffId = requireText(staffId, "staffId");
            if (start == null || end == null || !end.isAfter(start)) {
                throw new IllegalArgumentException("Shift end must be after its start.");
            }
            this.start = start;
            this.end = end;
        }

        public String getStaffId() { return staffId; }
        public LocalDateTime getStart() { return start; }
        public LocalDateTime getEnd() { return end; }
        private boolean covers(LocalDateTime windowStart, LocalDateTime windowEnd) {
            return !start.isAfter(windowStart) && !end.isBefore(windowEnd);
        }
    }

    public static final class Requirement {
        private final String name;
        private final LocalDateTime start;
        private final LocalDateTime end;
        private final int minimumStaff;

        public Requirement(String name, LocalDateTime start, LocalDateTime end, int minimumStaff) {
            this.name = requireText(name, "name");
            if (start == null || end == null || !end.isAfter(start)) {
                throw new IllegalArgumentException("Requirement end must be after its start.");
            }
            if (minimumStaff < 0) {
                throw new IllegalArgumentException("minimumStaff cannot be negative.");
            }
            this.start = start;
            this.end = end;
            this.minimumStaff = minimumStaff;
        }

        public String getName() { return name; }
        public LocalDateTime getStart() { return start; }
        public LocalDateTime getEnd() { return end; }
        public int getMinimumStaff() { return minimumStaff; }
    }

    public static final class Forecast {
        private final Requirement requirement;
        private final int scheduledStaff;
        private final int gap;

        private Forecast(Requirement requirement, int scheduledStaff, int gap) {
            this.requirement = requirement;
            this.scheduledStaff = scheduledStaff;
            this.gap = gap;
        }

        public Requirement getRequirement() { return requirement; }
        public int getScheduledStaff() { return scheduledStaff; }
        public int getGap() { return gap; }
        public boolean hasGap() { return gap > 0; }
    }

    public List<Forecast> forecast(List<Requirement> requirements, List<Shift> shifts) {
        List<Forecast> result = new ArrayList<Forecast>();
        if (requirements == null) {
            return result;
        }
        List<Shift> safeShifts = shifts == null ? Collections.<Shift>emptyList() : shifts;
        for (Requirement requirement : requirements) {
            if (requirement == null) {
                continue;
            }
            Set<String> coveredStaff = new LinkedHashSet<String>();
            for (Shift shift : safeShifts) {
                if (shift != null && shift.covers(requirement.getStart(), requirement.getEnd())) {
                    coveredStaff.add(shift.getStaffId());
                }
            }
            int covered = coveredStaff.size();
            result.add(new Forecast(requirement, covered,
                    Math.max(0, requirement.getMinimumStaff() - covered)));
        }
        return Collections.unmodifiableList(result);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
        return value.trim();
    }
}
