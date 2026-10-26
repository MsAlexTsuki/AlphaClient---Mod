package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores replay review rules as local metadata; it does not record or render replays. */
public final class InvestigationReplayRules {
    public static final class Rule {
        private final String name;
        private final String condition;
        private final String action;

        public Rule(String name, String condition, String action) {
            this.name = name == null ? "" : name.trim();
            this.condition = condition == null ? "" : condition.trim();
            this.action = action == null ? "" : action.trim();
        }
        public String getName() { return name; }
        public String getCondition() { return condition; }
        public String getAction() { return action; }
    }

    private final List<Rule> rules = new ArrayList<Rule>();
    public synchronized void add(Rule rule) { if (rule != null) rules.add(rule); }
    public synchronized List<Rule> getRules() {
        return Collections.unmodifiableList(new ArrayList<Rule>(rules));
    }
    public synchronized void clear() { rules.clear(); }
}