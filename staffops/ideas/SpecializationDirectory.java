package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Maintains a searchable directory of staff specializations.
 *
 * The directory is local state and does not verify claimed skills.
 * TODO(Supabase): persist approved specialization records and restrict edits
 * to authorized staff/management roles.
 * Compatible with Java 8.
 */
public class SpecializationDirectory {
    private final Map<String, Set<String>> specialtiesByStaff =
            new LinkedHashMap<String, Set<String>>();

    public void addSpecialization(String staffId, String specialization) {
        String id = requireText(staffId, "staffId");
        String specialty = normalizeSpecialty(specialization);
        Set<String> specialties = specialtiesByStaff.get(id);
        if (specialties == null) {
            specialties = new LinkedHashSet<String>();
            specialtiesByStaff.put(id, specialties);
        }
        specialties.add(specialty);
    }

    public void removeSpecialization(String staffId, String specialization) {
        String id = requireText(staffId, "staffId");
        Set<String> specialties = specialtiesByStaff.get(id);
        if (specialties != null) {
            specialties.remove(normalizeSpecialty(specialization));
            if (specialties.isEmpty()) {
                specialtiesByStaff.remove(id);
            }
        }
    }

    public List<String> findStaffBySpecialization(String specialization) {
        String query = normalizeSpecialty(specialization);
        List<String> matches = new ArrayList<String>();
        for (Map.Entry<String, Set<String>> entry : specialtiesByStaff.entrySet()) {
            if (entry.getValue().contains(query)) {
                matches.add(entry.getKey());
            }
        }
        Collections.sort(matches, String.CASE_INSENSITIVE_ORDER);
        return Collections.unmodifiableList(matches);
    }

    public Set<String> getSpecializations(String staffId) {
        Set<String> specialties = specialtiesByStaff.get(requireText(staffId, "staffId"));
        if (specialties == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new LinkedHashSet<String>(specialties));
    }

    public Map<String, Set<String>> getSnapshot() {
        Map<String, Set<String>> snapshot = new LinkedHashMap<String, Set<String>>();
        for (Map.Entry<String, Set<String>> entry : specialtiesByStaff.entrySet()) {
            snapshot.put(entry.getKey(),
                    Collections.unmodifiableSet(new LinkedHashSet<String>(entry.getValue())));
        }
        return Collections.unmodifiableMap(snapshot);
    }

    private static String normalizeSpecialty(String value) {
        return requireText(value, "specialization").toLowerCase(java.util.Locale.ROOT);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
        return value.trim();
    }
}
