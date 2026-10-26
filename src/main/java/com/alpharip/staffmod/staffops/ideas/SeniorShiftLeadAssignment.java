package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Assigns one senior staff lead to each shift.
 *
 * In-memory client-side model only; it does not verify rank or availability.
 * TODO(Supabase): persist assignments and enforce senior-only write access.
 * Compatible with Java 8.
 */
public class SeniorShiftLeadAssignment {
    private final Map<String, String> leadsByShift = new LinkedHashMap<String, String>();

    public void assignLead(String shiftId, String staffId) {
        leadsByShift.put(requireText(shiftId, "shiftId"), requireText(staffId, "staffId"));
    }

    public String getLead(String shiftId) {
        return leadsByShift.get(requireText(shiftId, "shiftId"));
    }

    public boolean hasLead(String shiftId) {
        return leadsByShift.containsKey(requireText(shiftId, "shiftId"));
    }

    public void removeLead(String shiftId) {
        leadsByShift.remove(requireText(shiftId, "shiftId"));
    }

    public Map<String, String> getAssignments() {
        return Collections.unmodifiableMap(new LinkedHashMap<String, String>(leadsByShift));
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
        return value.trim();
    }
}
