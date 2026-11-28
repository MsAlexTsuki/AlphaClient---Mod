package com.alpharip.staffmod.aimpatternlibrary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Coordinates pattern management and read-only statistics. */
public final class AimPatternService {
    private final AimPatternRegistry registry;
    private final AimPatternSettings settings;

    public AimPatternService(AimPatternRegistry registry, AimPatternSettings settings) {
        if (registry == null || settings == null) throw new IllegalArgumentException("Dependencies cannot be null");
        this.registry = registry;
        this.settings = settings;
    }
    public AimPattern create(String name, AimPatternCategory category) {
        AimPattern created = registry.create(name, category);
        settings.setSelectedPatternId(created.getId());
        return created;
    }
    public boolean record(String id, double observation) { return registry.addObservation(id, observation); }
    public boolean rename(String id, String name) { return registry.rename(id, name); }
    public boolean categorize(String id, AimPatternCategory category) { return registry.setCategory(id, category); }
    public boolean delete(String id) {
        boolean removed = registry.remove(id);
        if (removed && id.equals(settings.getSelectedPatternId())) settings.setSelectedPatternId(null);
        return removed;
    }
    public List<AimPattern> listVisible() {
        List<AimPattern> result = new ArrayList<AimPattern>();
        for (AimPattern pattern : registry.snapshot()) {
            if (settings.getFilter() == null || settings.getFilter() == pattern.getCategory()) result.add(pattern);
        }
        return Collections.unmodifiableList(result);
    }
    public AimPatternStats statistics(String id) {
        return AimPatternStats.calculate(registry.get(id));
    }
    public void clearAll() {
        registry.clear();
        settings.setSelectedPatternId(null);
    }
}
