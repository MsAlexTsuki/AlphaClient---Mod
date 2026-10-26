package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Local criteria-to-priority mappings; evaluation and server enforcement are outside this model. */
public final class InvestigationPriorityRules {
    public static final class Rule {
        private final String condition;
        private final int priority;

        public Rule(String condition, int priority) {
            this.condition = condition == null ? "" : condition.trim();
            this.priority = priority;
        }
        public String getCondition() { return condition; }
        public int getPriority() { return priority; }
    }

    private final List<Rule> rules = new ArrayList<Rule>();

    public synchronized void add(String condition, int priority) {
        if (condition != null && !condition.trim().isEmpty()) rules.add(new Rule(condition, priority));
    }
    public synchronized List<Rule> getRules() {
        return Collections.unmodifiableList(new ArrayList<Rule>(rules));
    }
    public synchronized void clear() { rules.clear(); }
}