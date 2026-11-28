package com.alpharip.staffmod.caseescalationtimer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** In-memory case registry. Persistence is delegated to EscalationStorage. */
public final class EscalationTracker {
    private final Map<String, EscalationCase> cases = new LinkedHashMap<String, EscalationCase>();

    public synchronized boolean add(EscalationCase item) {
        if (item == null || cases.containsKey(item.getId())) return false;
        cases.put(item.getId(), item);
        return true;
    }

    public synchronized boolean replace(EscalationCase item) {
        if (item == null || !cases.containsKey(item.getId())) return false;
        cases.put(item.getId(), item);
        return true;
    }

    public synchronized EscalationCase get(String id) { return cases.get(id); }
    public synchronized boolean remove(String id) { return cases.remove(id) != null; }
    public synchronized List<EscalationCase> getCases() {
        return Collections.unmodifiableList(new ArrayList<EscalationCase>(cases.values()));
    }
    public synchronized void replaceAll(List<EscalationCase> values) {
        cases.clear();
        if (values == null) return;
        for (EscalationCase item : values) if (item != null && !cases.containsKey(item.getId())) cases.put(item.getId(), item);
    }
    public synchronized void clear() { cases.clear(); }
}
