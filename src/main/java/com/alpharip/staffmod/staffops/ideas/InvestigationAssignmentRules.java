package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Local assignment criteria and intended assignee metadata; no remote assignment occurs. */
public final class InvestigationAssignmentRules {
    public static final class Rule {
        private final String condition;
        private final String assignee;

        public Rule(String condition, String assignee) {
            this.condition = condition == null ? "" : condition.trim();
            this.assignee = assignee == null ? "" : assignee.trim();
        }
        public String getCondition() { return condition; }
        public String getAssignee() { return assignee; }
    }

    private final List<Rule> rules = new ArrayList<Rule>();

    public synchronized void add(Rule rule) { if (rule != null) rules.add(rule); }
    public synchronized List<Rule> getRules() {
        return Collections.unmodifiableList(new ArrayList<Rule>(rules));
    }
    public synchronized void clear() { rules.clear(); }
}