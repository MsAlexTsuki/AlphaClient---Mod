package com.alpharip.staffmod.staffops.ideas;
import java.util.EnumMap;
import java.util.Map;
/** Per-armor-piece durability estimate; equipment tracking and HUD silhouette are pending. */
public final class HitsLeft {
    public enum ArmorSlot { HELMET, CHESTPLATE, LEGGINGS, BOOTS }
    public static final class Estimate {
        public final int remainingDurability, estimatedHitsLeft;
        public Estimate(int remainingDurability, int estimatedHitsLeft) {
            this.remainingDurability = remainingDurability; this.estimatedHitsLeft = estimatedHitsLeft;
        }
    }
    private final Map<ArmorSlot, Estimate> estimates = new EnumMap<ArmorSlot, Estimate>(ArmorSlot.class);
    public void update(ArmorSlot slot, int maximumDurability, int accumulatedDamage, int averageDamagePerHit) {
        if (slot == null) return;
        int remaining = Math.max(0, maximumDurability - Math.max(0, accumulatedDamage));
        int hits = averageDamagePerHit <= 0 ? 0 : (int)Math.ceil((double)remaining / averageDamagePerHit);
        estimates.put(slot, new Estimate(remaining, hits));
    }
    public Estimate get(ArmorSlot slot) { return estimates.get(slot); }
    public Map<ArmorSlot, Estimate> getAll() { return new EnumMap<ArmorSlot, Estimate>(estimates); }
    public void clear() { estimates.clear(); }
}
