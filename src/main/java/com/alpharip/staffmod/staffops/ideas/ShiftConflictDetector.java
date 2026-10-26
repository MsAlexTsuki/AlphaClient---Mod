package com.alpharip.staffmod.staffops.ideas;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Detects overlapping shifts assigned to the same staff member.
 *
 * This class is an in-memory client-side domain model. It does not read live
 * staff schedules or enforce permissions.
 * TODO(Supabase): persist shared schedules and enforce staff access policies
 * before using this with multiple staff accounts.
 * Compatible with Java 8.
 */
public class ShiftConflictDetector {
    public static final class Shift {
        private final String id;
        private final String staffId;
        private final LocalDateTime start;
        private final LocalDateTime end;

        public Shift(String id, String staffId, LocalDateTime start, LocalDateTime end) {
            this.id = requireText(id, "id");
            this.staffId = requireText(staffId, "staffId");
            if (start == null || end == null || !end.isAfter(start)) {
                throw new IllegalArgumentException("Shift end must be after its start.");
            }
            this.start = start;
            this.end = end;
        }

        public String getId() { return id; }
        public String getStaffId() { return staffId; }
        public LocalDateTime getStart() { return start; }
        public LocalDateTime getEnd() { return end; }

        private boolean overlaps(Shift other) {
            return start.isBefore(other.end) && end.isAfter(other.start);
        }
    }

    public static final class Conflict {
        private final Shift first;
        private final Shift second;

        private Conflict(Shift first, Shift second) {
            this.first = first;
            this.second = second;
        }

        public Shift getFirst() { return first; }
        public Shift getSecond() { return second; }
    }

    public List<Conflict> detect(List<Shift> shifts) {
        List<Conflict> conflicts = new ArrayList<Conflict>();
        if (shifts == null) {
            return conflicts;
        }

        for (int i = 0; i < shifts.size(); i++) {
            Shift first = shifts.get(i);
            if (first == null) {
                continue;
            }
            for (int j = i + 1; j < shifts.size(); j++) {
                Shift second = shifts.get(j);
                if (second == null || first.getId().equals(second.getId())) {
                    continue;
                }
                if (first.getStaffId().equals(second.getStaffId()) && first.overlaps(second)) {
                    conflicts.add(new Conflict(first, second));
                }
            }
        }
        return Collections.unmodifiableList(conflicts);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
        return value.trim();
    }
}
