package com.alpharip.staffmod.staffops.ideas;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Simple local text search helper for supplied timeline descriptions. */
public final class EvidenceTimelineSearch {
    public List<Integer> findMatches(List<String> descriptions, String query) {
        List<Integer> matches = new ArrayList<Integer>();
        if (descriptions == null || query == null || query.trim().isEmpty()) {
            return Collections.unmodifiableList(matches);
        }
        String needle = query.trim().toLowerCase(java.util.Locale.ROOT);
        for (int i = 0; i < descriptions.size(); i++) {
            String value = descriptions.get(i);
            if (value != null && value.toLowerCase(java.util.Locale.ROOT).contains(needle)) {
                matches.add(i);
            }
        }
        return Collections.unmodifiableList(matches);
    }
}