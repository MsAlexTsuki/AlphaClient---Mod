package com.alpharip.staffmod.appealconflictdetector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Coordinates registry access and conflict evaluation. */
public final class AppealConflictService {
    private final AppealConflictRegistry registry;
    private final AppealConflictPolicy policy;
    private final AppealConflictDetector detector;

    public AppealConflictService(AppealConflictRegistry registry, AppealConflictPolicy policy) {
        if (registry == null || policy == null) throw new IllegalArgumentException("registry and policy are required");
        this.registry = registry;
        this.policy = policy;
        this.detector = new AppealConflictDetector();
    }

    public AppealConflictReport evaluate(String caseId) {
        AppealCase appealCase = registry.get(caseId);
        if (appealCase == null) return null;
        return detector.evaluate(appealCase, policy);
    }

    public List<AppealConflictReport> evaluateAll() {
        List<AppealConflictReport> reports = new ArrayList<AppealConflictReport>();
        for (AppealCase appealCase : registry.snapshot()) {
            reports.add(detector.evaluate(appealCase, policy));
        }
        return Collections.unmodifiableList(reports);
    }

    public AppealConflictRegistry getRegistry() { return registry; }
    public AppealConflictPolicy getPolicy() { return policy; }
}
