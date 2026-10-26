package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores configured status transition rules without enforcing workflow transitions. */
public final class InvestigationStatusRules {
    public static final class Rule {
        private final String fromStatus;
        private final String toStatus;
        private final String condition;

        public Rule(String fromStatus, String toStatus, String condition) {
            this.fromStatus = fromStatus == null ? "" : fromStatus.trim();
            this.toStatus = toStatus == null ? "" : toStatus.trim();
            this.condition = condition == null ? "" : condition.trim();
        }
        public String getFromStatus() { return fromStatus; }
        public String getToStatus() { return toStatus; }
        public String getCondition() { return condition; }
    }

    private final List<Rule> rules = new ArrayList<Rule>();

    public synchronized void add(Rule rule) { if (rule != null) rules.add(rule); }
    public synchronized List<Rule> getRules() {
        return Collections.unmodifiableList(new ArrayList<Rule>(rules));
    }
    public synchronized void clear() { rules.clear(); }
}