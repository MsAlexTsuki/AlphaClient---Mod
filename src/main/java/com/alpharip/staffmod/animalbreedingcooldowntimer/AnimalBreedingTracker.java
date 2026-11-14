package com.alpharip.staffmod.animalbreedingcooldowntimer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** In-memory per-animal breeding cooldown registry. */
public final class AnimalBreedingTracker {
    private static final int MAX_ENTRIES = 256;
    private final Map<UUID, AnimalBreedingEntry> entries =
            new LinkedHashMap<UUID, AnimalBreedingEntry>();

    public synchronized void record(UUID animalId, String displayName,
                                     long nowMillis, int cooldownSeconds) {
        if (animalId == null) return;
        entries.remove(animalId);
        entries.put(animalId, new AnimalBreedingEntry(
                animalId, displayName, nowMillis, cooldownSeconds));
        while (entries.size() > MAX_ENTRIES) {
            UUID oldest = entries.keySet().iterator().next();
            entries.remove(oldest);
        }
    }

    public synchronized AnimalBreedingEntry get(UUID animalId) {
        return entries.get(animalId);
    }

    public synchronized List<AnimalBreedingEntry> getAll() {
        return Collections.unmodifiableList(
                new ArrayList<AnimalBreedingEntry>(entries.values()));
    }

    public synchronized int size() {
        return entries.size();
    }

    public synchronized void clear() {
        entries.clear();
    }

    synchronized void replaceAll(Collection<AnimalBreedingEntry> values) {
        entries.clear();
        if (values == null) return;
        for (AnimalBreedingEntry entry : values) {
            if (entry != null) record(entry.getAnimalId(), entry.getDisplayName(),
                    entry.getLastBredAtMillis(), entry.getCooldownSeconds());
        }
    }
}
