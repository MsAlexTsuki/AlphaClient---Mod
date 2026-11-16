package com.alpharip.staffmod.appealconflictdetector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** In-memory case registry for prototype input; it does not fetch or persist server data. */
public final class AppealConflictRegistry {
    private final Map<String, AppealCase> cases = new LinkedHashMap<String, AppealCase>();

    public synchronized void put(AppealCase appealCase) {
        if (appealCase == null) throw new IllegalArgumentException("appealCase is required");
        cases.put(appealCase.getCaseId(), appealCase);
    }

    public synchronized AppealCase get(String caseId) {
        return caseId == null ? null : cases.get(caseId);
    }

    public synchronized boolean remove(String caseId) {
        return caseId != null && cases.remove(caseId) != null;
    }

    public synchronized List<AppealCase> snapshot() {
        return Collections.unmodifiableList(new ArrayList<AppealCase>(cases.values()));
    }

    public synchronized void clear() {
        cases.clear();
    }
}
