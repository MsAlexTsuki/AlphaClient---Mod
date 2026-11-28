package com.alpharip.staffmod.store.cosmetics;

import java.util.Set;

/**
 * Coordinates catalogue entries, trusted ownership snapshots, and equipped loadouts.
 * Purchase/payment processing and remote entitlement synchronization are external.
 */
public final class CosmeticManager {
    private final CosmeticRegistry catalogue;
    private final CosmeticOwnershipRegistry ownership;
    private final CosmeticLoadouts loadouts;

    public CosmeticManager(CosmeticRegistry catalogue) {
        this(catalogue, new CosmeticOwnershipRegistry(), new CosmeticLoadouts());
    }

    public CosmeticManager(CosmeticRegistry catalogue,
                           CosmeticOwnershipRegistry ownership,
                           CosmeticLoadouts loadouts) {
        if (catalogue == null || ownership == null || loadouts == null)
            throw new IllegalArgumentException("Cosmetic manager dependencies are required.");
        this.catalogue = catalogue;
        this.ownership = ownership;
        this.loadouts = loadouts;
    }

    /** Replace ownership only with IDs returned by a trusted entitlement source. */
    public synchronized void applyTrustedOwnershipSnapshot(Iterable<String> ownedIds) {
        ownership.replaceOwnedIds(ownedIds, catalogue);
        for (CosmeticCategory category : CosmeticCategory.values()) {
            String equippedId = loadouts.getEquippedId(category);
            if (equippedId != null && !ownership.isOwned(equippedId)) {
                loadouts.unequip(category);
            }
        }
    }

    public synchronized boolean equip(String cosmeticId) {
        CosmeticEntry entry = catalogue.get(cosmeticId);
        return entry != null && ownership.isOwned(cosmeticId) && loadouts.equip(entry);
    }

    public synchronized boolean unequip(String cosmeticId) {
        CosmeticEntry entry = catalogue.get(cosmeticId);
        return entry != null && loadouts.unequip(entry.getCategory());
    }

    public synchronized boolean isOwned(String cosmeticId) {
        return ownership.isOwned(cosmeticId);
    }

    public synchronized boolean isEquipped(String cosmeticId) {
        return loadouts.isEquipped(cosmeticId);
    }

    public synchronized String getEquippedId(CosmeticCategory category) {
        return loadouts.getEquippedId(category);
    }

    public synchronized Set<String> getOwnedIds() {
        return ownership.getOwnedIds();
    }

    public synchronized void clearLocalSession() {
        ownership.clear();
        loadouts.clear();
    }
}
