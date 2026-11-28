package com.alpharip.staffmod.actingrankassignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Coordinates temporary acting-rank records without changing effective permissions. */
public final class ActingRankService {
    private final ActingRankRegistry registry;
    private final ActingRankSettings settings;

    public ActingRankService(ActingRankRegistry registry, ActingRankSettings settings) {
        if (registry == null || settings == null) throw new IllegalArgumentException("Registry and settings are required.");
        this.registry = registry;
        this.settings = settings;
        this.registry.setMaximumEntries(settings.getMaximumEntries());
    }

    public ActingRankEntry create(String staffName, String currentRank, String actingRank,
            String assignedBy, String reason, long durationMillis, long nowMillis) {
        if (!settings.isEnabled()) throw new IllegalStateException("Acting-rank tracking is disabled.");
        if (durationMillis <= 0L || nowMillis > Long.MAX_VALUE - durationMillis) {
            throw new IllegalArgumentException("Duration is invalid.");
        }
        long expiresAt = nowMillis + durationMillis;
        String error = ActingRankValidator.validate(staffName, currentRank, actingRank,
                assignedBy, reason, nowMillis, expiresAt);
        if (error != null) throw new IllegalArgumentException(error);

        ActingRankEntry entry = new ActingRankEntry(staffName.trim(), currentRank.trim(),
                actingRank.trim(), assignedBy.trim(), reason == null ? "" : reason.trim(),
                nowMillis, expiresAt);
        if (!registry.add(entry)) throw new IllegalStateException("The registry is full.");
        return entry;
    }

    public boolean revoke(String id, String revokedBy, long nowMillis) {
        if (revokedBy == null || revokedBy.trim().isEmpty()) return false;
        ActingRankEntry entry = registry.get(id);
        if (entry == null || !entry.isActiveAt(nowMillis)) return false;
        return registry.replace(entry.revoke(revokedBy.trim(), nowMillis));
    }

    public void refreshExpired(long nowMillis) {
        for (ActingRankEntry entry : registry.getAll()) {
            if (entry.getStatus() == ActingRankStatus.ACTIVE
                    && entry.getExpiresAtMillis() <= nowMillis) {
                registry.replace(entry.markExpired());
            }
        }
    }

    public List<ActingRankEntry> search(String query, boolean includeExpired, long nowMillis) {
        refreshExpired(nowMillis);
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<ActingRankEntry> result = new ArrayList<ActingRankEntry>();
        for (ActingRankEntry entry : registry.getAll()) {
            if (!includeExpired && entry.getEffectiveStatus(nowMillis) != ActingRankStatus.ACTIVE) continue;
            String haystack = (entry.getStaffName() + " " + entry.getCurrentRank() + " "
                    + entry.getActingRank() + " " + entry.getAssignedBy() + " "
                    + entry.getReason()).toLowerCase(Locale.ROOT);
            if (needle.isEmpty() || haystack.contains(needle)) result.add(entry);
        }
        return Collections.unmodifiableList(result);
    }

    public ActingRankRegistry getRegistry() { return registry; }
    public ActingRankSettings getSettings() { return settings; }
}
