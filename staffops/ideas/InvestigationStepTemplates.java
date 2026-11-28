package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Local named templates containing reusable investigation step lists. */
public final class InvestigationStepTemplates {
    private final Map<String, List<String>> templates = new LinkedHashMap<String, List<String>>();

    public synchronized void save(String name, List<String> steps) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("template name is required");
        List<String> copy = new ArrayList<String>();
        if (steps != null) for (String step : steps) if (step != null && !step.trim().isEmpty()) copy.add(step.trim());
        templates.put(name.trim(), copy);
    }

    public synchronized List<String> get(String name) {
        List<String> steps = templates.get(name);
        return steps == null ? Collections.<String>emptyList()
            : Collections.unmodifiableList(new ArrayList<String>(steps));
    }

    public synchronized boolean delete(String name) { return templates.remove(name) != null; }

    public synchronized List<String> getNames() {
        return Collections.unmodifiableList(new ArrayList<String>(templates.keySet()));
    }
}