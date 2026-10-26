package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Local category list for organizing evidence references. */
public final class EvidenceCategories {
    private final List<String> categories = new ArrayList<String>();

    public boolean add(String category) {
        if (category == null || category.trim().isEmpty()) return false;
        String value = category.trim();
        for (String existing : categories) {
            if (existing.equalsIgnoreCase(value)) return false;
        }
        categories.add(value);
        return true;
    }

    public List<String> getAll() {
        return Collections.unmodifiableList(new ArrayList<String>(categories));
    }
}