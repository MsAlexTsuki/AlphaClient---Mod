package com.alpharip.staffmod.staffops.ideas;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Compares locally supplied role labels without granting or inferring access.
 *
 * TODO(Supabase): load canonical role-permission mappings from the agreed
 * permission source; do not treat role-name ordering as authorization.
 */
public final class StaffRoleComparison {
    public static final String FEATURE_NAME = "Staff Role Comparison";

    public Set<String> findDifferences(Set<String> first, Set<String> second) {
        Set<String> differences = new LinkedHashSet<String>();
        if (first != null) {
            differences.addAll(first);
        }
        if (second != null) {
            for (String value : second) {
                if (!differences.add(value)) {
                    differences.remove(value);
                }
            }
        }
        return differences;
    }
}
