package com.alpharip.staffmod.appealconflictdetector;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Explicit conflict policy. It starts empty so the prototype does not invent
 * Alpha's staff rules; callers must add the involvement types that should block review.
 */
public final class AppealConflictPolicy {
    private final Map<AppealInvolvementType, AppealConflictRule> rules =
            new LinkedHashMap<AppealInvolvementType, AppealConflictRule>();

    public synchronized void putRule(AppealConflictRule rule) {
        if (rule == null) throw new IllegalArgumentException("rule is required");
        rules.put(rule.getInvolvementType(), rule);
    }

    public synchronized void removeRule(AppealInvolvementType type) {
        if (type != null) rules.remove(type);
    }

    public synchronized AppealConflictRule getRule(AppealInvolvementType type) {
        return rules.get(type);
    }

    public synchronized Map<AppealInvolvementType, AppealConflictRule> snapshot() {
        return Collections.unmodifiableMap(
                new LinkedHashMap<AppealInvolvementType, AppealConflictRule>(rules));
    }
}
