package com.alpharip.staffdashboardexpansion.rankcomp;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
/** Immutable result split into shared and role-specific permission labels. */
public final class StaffRankComparisonResult {
    private final Set<String> shared, firstOnly, secondOnly;
    StaffRankComparisonResult(Set<String> shared, Set<String> firstOnly, Set<String> secondOnly) {
        this.shared = copy(shared); this.firstOnly = copy(firstOnly); this.secondOnly = copy(secondOnly);
    }
    private static Set<String> copy(Set<String> source) {
        return Collections.unmodifiableSet(new LinkedHashSet<String>(source));
    }
    public Set<String> getShared() { return shared; }
    public Set<String> getFirstOnly() { return firstOnly; }
    public Set<String> getSecondOnly() { return secondOnly; }
    public int getComparedPermissionCount() { return shared.size() + firstOnly.size() + secondOnly.size(); }
}
