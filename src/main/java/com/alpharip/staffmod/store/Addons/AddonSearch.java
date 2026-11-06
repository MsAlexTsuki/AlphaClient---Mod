package com.alpharip.staffmod.store.addons;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Deterministic local search/filter over supplied catalogue entries. */
public final class AddonSearch {
    public List<AddonEntry> search(List<AddonEntry> entries, String query, AddonCategory category) {
        List<AddonEntry> result = new ArrayList<AddonEntry>();
        if (entries == null) return result;
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        for (AddonEntry entry : entries) {
            if (entry == null) continue;
            if (category != null && category != AddonCategory.ALL && entry.getCategory() != category) continue;
            String searchable = (entry.getName() + " " + entry.getDescription() + " "
                    + entry.getAuthor() + " " + entry.getId()).toLowerCase(Locale.ROOT);
            if (needle.isEmpty() || searchable.contains(needle)) result.add(entry);
        }
        return result;
    }
}
