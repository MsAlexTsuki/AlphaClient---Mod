package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** Stores identifiers explicitly marked for special review by configuration. */
public class DetectionBlacklist {
    private final Set<String> identifiers = new HashSet<String>();

    public boolean add(String identifier) {
        return identifier != null && !identifier.trim().isEmpty()
                && identifiers.add(identifier.trim());
    }

    public boolean remove(String identifier) {
        return identifier != null && identifiers.remove(identifier.trim());
    }

    public boolean contains(String identifier) {
        return identifier != null && identifiers.contains(identifier.trim());
    }

    public Set<String> getIdentifiers() {
        return Collections.unmodifiableSet(new HashSet<String>(identifiers));
    }

    public void clear() {
        identifiers.clear();
    }
}
