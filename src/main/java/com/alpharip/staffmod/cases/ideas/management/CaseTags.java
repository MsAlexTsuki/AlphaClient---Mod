package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Local tags for categorizing an investigation case. */
public final class CaseTags {
    private final List<String> tags = new ArrayList<String>();

    public boolean add(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return false;
        }
        String normalized = tag.trim();
        for (String existing : tags) {
            if (existing.equalsIgnoreCase(normalized)) {
                return false;
            }
        }
        return tags.add(normalized);
    }

    public boolean remove(String tag) {
        if (tag == null) return false;
        for (int i = 0; i < tags.size(); i++) {
            if (tags.get(i).equalsIgnoreCase(tag.trim())) {
                tags.remove(i);
                return true;
            }
        }
        return false;
    }

    public List<String> getTags() {
        return Collections.unmodifiableList(new ArrayList<String>(tags));
    }
}