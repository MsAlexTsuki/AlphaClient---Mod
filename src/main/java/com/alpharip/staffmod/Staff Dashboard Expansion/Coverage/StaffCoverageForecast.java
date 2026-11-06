package com.alpharip.staffdashboardexpansion.coverage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Locale;

import com.alpharip.staffdashboardexpansion.shifts.StaffShift;
import com.alpharip.staffdashboardexpansion.shifts.StaffShiftManager;
import com.alpharip.staffdashboardexpansion.shifts.StaffShiftStatus;

/** Calculates planned coverage from the existing local Staff Shift Board. */
public final class StaffCoverageForecast {
    private final List<CoverageRequirement> requirements = new ArrayList<CoverageRequirement>();
    private final StaffShiftManager shiftManager;
    private long nextId = 1L;

    public StaffCoverageForecast(StaffShiftManager shiftManager) {
        if (shiftManager == null) throw new IllegalArgumentException("shiftManager cannot be null.");
        this.shiftManager = shiftManager;
    }

    public synchronized CoverageRequirement addRequirement(String name, LocalDateTime start,
                                                              LocalDateTime end, int minimumStaff) {
        CoverageRequirement requirement = new CoverageRequirement(String.valueOf(nextId++), name,
                start, end, minimumStaff);
        requirements.add(requirement);
        return requirement;
    }

    public synchronized boolean removeRequirement(String id) {
        if (id == null) return false;
        for (int i = 0; i < requirements.size(); i++) {
            if (requirements.get(i).getId().equals(id)) {
                requirements.remove(i);
                return true;
            }
        }
        return false;
    }

    public synchronized List<CoverageRequirement> getRequirements() {
        return Collections.unmodifiableList(new ArrayList<CoverageRequirement>(requirements));
    }

    public synchronized List<CoverageResult> forecast(LocalDate date) {
        List<CoverageResult> results = new ArrayList<CoverageResult>();
        if (date == null) return Collections.unmodifiableList(results);
        List<StaffShift> shifts = shiftManager.getBoard().getShifts();
        for (CoverageRequirement requirement : requirements) {
            if (!requirement.getStart().toLocalDate().equals(date)) continue;
            Set<String> covered = new LinkedHashSet<String>();
            for (StaffShift shift : shifts) {
                if (shift.getStatus() == StaffShiftStatus.CANCELLED) continue;
                if (!shift.getStart().isAfter(requirement.getStart())
                        && !shift.getEnd().isBefore(requirement.getEnd())) {
                    covered.add(shift.getStaffName().toLowerCase(Locale.ROOT));
                }
            }
            results.add(new CoverageResult(requirement, covered.size()));
        }
        return Collections.unmodifiableList(results);
    }

    // TODO(Supabase): shared authorized requirements require explicit integration approval.
    // TODO: Dashboard host remains responsible for staff identity and permission checks.
}