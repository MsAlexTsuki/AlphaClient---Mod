package com.alpharip.staffdashboardexpansion.rankcomp;
/** Standalone in-memory facade. No dashboard registration or authorization integration is included. */
public final class StaffRankComparisonModule {
    public static final String FEATURE_NAME = "Rank Comp";
    private final StaffRankComparison comparison;
    public StaffRankComparisonModule() { this(new StaffRankComparison()); }
    public StaffRankComparisonModule(StaffRankComparison comparison) {
        if (comparison == null) throw new IllegalArgumentException("Comparison service is required.");
        this.comparison = comparison;
    }
    public StaffRankComparisonResult compare(String firstRole, String firstPermissions,
                                               String secondRole, String secondPermissions) {
        return comparison.compare(new StaffRankProfile(firstRole, firstPermissions),
                new StaffRankProfile(secondRole, secondPermissions));
    }
    public StaffRankComparison getComparison() { return comparison; }
    // TODO(Supabase): load approved role-permission mappings from the canonical source.
    // Never infer authorization from rank names or this local comparison result.
}
