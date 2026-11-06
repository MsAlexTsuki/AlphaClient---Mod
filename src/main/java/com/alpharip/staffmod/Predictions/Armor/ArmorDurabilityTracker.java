package com.alpharip.staffmod.predictions.armor;

import java.util.ArrayDeque;
import java.util.Deque;

/** Tracks observed durability loss for one opponent and one armor slot. */
final class ArmorDurabilityTracker {
    private final String opponentName;
    private final ArmorSlot slot;
    private final int maximumDurability;
    private final int maxSamples;
    private final Deque<Integer> recentDamage = new ArrayDeque<Integer>();
    private int accumulatedDamage;

    ArmorDurabilityTracker(String opponentName, ArmorSlot slot, int maximumDurability, int maxSamples) {
        this.opponentName = opponentName;
        this.slot = slot;
        this.maximumDurability = maximumDurability;
        this.maxSamples = maxSamples;
    }

    void recordDamage(int amount) {
        if (amount <= 0) return;
        accumulatedDamage = (int) Math.min((long) maximumDurability, (long) accumulatedDamage + amount);
        recentDamage.addLast(amount);
        while (recentDamage.size() > maxSamples) recentDamage.removeFirst();
    }

    ArmorBreakResult createResult(int minimumSamples) {
        int remaining = Math.max(0, maximumDurability - accumulatedDamage);
        double average = 0.0D;
        for (Integer sample : recentDamage) average += sample;
        if (!recentDamage.isEmpty()) average /= recentDamage.size();

        boolean available = recentDamage.size() >= minimumSamples && average > 0.0D && remaining > 0;
        int hits = available ? (int) Math.ceil(remaining / average) : -1;
        double percent = maximumDurability == 0 ? 0.0D
                : (remaining * 100.0D) / maximumDurability;

        return new ArmorBreakResult(opponentName, slot, maximumDurability, accumulatedDamage,
                remaining, percent, average, hits, available, remaining == 0);
    }
}
