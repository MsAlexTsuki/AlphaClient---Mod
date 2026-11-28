package com.alpharip.staffdashboardexpansion.rankcomp;
import java.util.LinkedHashSet;
import java.util.Set;
/**
 * Compares two manually supplied role profiles. It reports labels only and
 * never resolves rank hierarchy or grants/revokes access.
 */
public final class StaffRankComparison {
    public StaffRankComparisonResult compare(StaffRankProfile first, StaffRankProfile second) {
        if (first == null || second == null) throw new IllegalArgumentException("Both role profiles are required.");
        Set<String> shared = new LinkedHashSet<String>(first.getPermissions());
        shared.retainAll(second.getPermissions());
        Set<String> firstOnly = new LinkedHashSet<String>(first.getPermissions());
        firstOnly.removeAll(second.getPermissions());
        Set<String> secondOnly = new LinkedHashSet<String>(second.getPermissions());
        secondOnly.removeAll(first.getPermissions());
        return new StaffRankComparisonResult(shared, firstOnly, secondOnly);
    }
}
