package com.alpharip.staffmod.store.cosmetics;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** Holds one equipped cosmetic per category for the active local player. */
public final class CosmeticLoadouts {
    private final Map<CosmeticCategory, String> equipped =
            new EnumMap<CosmeticCategory, String>(CosmeticCategory.class);

    public synchronized boolean equip(CosmeticEntry entry) {
        if (entry == null || entry.getCategory() == CosmeticCategory.ALL) return false;
        equipped.put(entry.getCategory(), entry.getId());
        return true;
    }

    public synchronized boolean unequip(CosmeticCategory category) {
        return category != null && category != CosmeticCategory.ALL
                && equipped.remove(category) != null;
    }

    public synchronized String getEquippedId(CosmeticCategory category) {
        return category == null ? null : equipped.get(category);
    }

    public synchronized boolean isEquipped(String cosmeticId) {
        return cosmeticId != null && equipped.containsValue(cosmeticId);
    }

    public synchronized Map<CosmeticCategory, String> snapshot() {
        return Collections.unmodifiableMap(new EnumMap<CosmeticCategory, String>(equipped));
    }

    public synchronized void clear() {
        equipped.clear();
    }
}
