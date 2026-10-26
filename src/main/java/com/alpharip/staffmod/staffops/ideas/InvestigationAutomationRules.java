package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores user-defined rule descriptions; does not execute actions automatically. */
public final class InvestigationAutomationRules {
    public static final class Rule {
        private final String name;
        private final String condition;
        private final String action;
        private final boolean enabled;

        public Rule(String name, String condition, String action, boolean enabled) {
            this.name = name == null ? "" : name.trim();
            this.condition = condition == null ? "" : condition.trim();
            this.action = action == null ? "" : action.trim();
            this.enabled = enabled;
        }
        public String getName() { return name; }
        public String getCondition() { return condition; }
        public String getAction() { return action; }
        public boolean isEnabled() { return enabled; }
    }

    private final List<Rule> rules = new ArrayList<Rule>();

    public synchronized void add(Rule rule) {
        if (rule != null) rules.add(rule);
    }
    public synchronized boolean removeAt(int index) {
        if (index < 0 || index >= rules.size()) return false;
        rules.remove(index);
        return true;
    }
    public synchronized List<Rule> getRules() {
        return Collections.unmodifiableList(new ArrayList<Rule>(rules));
    }
    public synchronized void clear() { rules.clear(); }
}