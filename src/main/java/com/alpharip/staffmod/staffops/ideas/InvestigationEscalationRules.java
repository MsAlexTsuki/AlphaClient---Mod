package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores escalation criteria as local configuration; it does not contact staff. */
public final class InvestigationEscalationRules {
    public static final class Rule {
        private final String name;
        private final String condition;
        private final String destinationRole;

        public Rule(String name, String condition, String destinationRole) {
            this.name = name == null ? "" : name.trim();
            this.condition = condition == null ? "" : condition.trim();
            this.destinationRole = destinationRole == null ? "" : destinationRole.trim();
        }
        public String getName() { return name; }
        public String getCondition() { return condition; }
        public String getDestinationRole() { return destinationRole; }
    }

    private final List<Rule> rules = new ArrayList<Rule>();

    public synchronized void add(Rule rule) { if (rule != null) rules.add(rule); }
    public synchronized List<Rule> getRules() {
        return Collections.unmodifiableList(new ArrayList<Rule>(rules));
    }
    public synchronized void clear() { rules.clear(); }
}