package com.alpharip.staffmod.store.cosmetics;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * In-memory ownership snapshot supplied by a trusted entitlement source.
 * This class does not grant purchases or validate payments.
 */
public final class CosmeticOwnershipRegistry {
    private final Set<String> ownedIds = new LinkedHashSet<String>();

    public synchronized void replaceOwnedIds(Iterable<String> ids, CosmeticRegistry catalogue) {
        if (catalogue == null) throw new IllegalArgumentException("Cosmetic catalogue is required.");
        ownedIds.clear();
        if (ids == null) return;
        for (String id : ids) {
            if (id != null && catalogue.get(id) != null) ownedIds.add(id);
        }
    }

    public synchronized boolean isOwned(String id) {
        return id != null && ownedIds.contains(id);
    }

    public synchronized Set<String> getOwnedIds() {
        return Collections.unmodifiableSet(new LinkedHashSet<String>(ownedIds));
    }

    public synchronized void clear() {
        ownedIds.clear();
    }
}
