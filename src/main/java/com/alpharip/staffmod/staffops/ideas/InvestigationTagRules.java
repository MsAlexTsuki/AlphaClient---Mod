package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Local condition-to-tag rule definitions; this class does not apply tags automatically. */
public final class InvestigationTagRules {
    public static final class Rule {
        private final String condition;
        private final String tag;

        public Rule(String condition, String tag) {
            this.condition = condition == null ? "" : condition.trim();
            this.tag = tag == null ? "" : tag.trim();
        }
        public String getCondition() { return condition; }
        public String getTag() { return tag; }
    }

    private final List<Rule> rules = new ArrayList<Rule>();

    public synchronized void add(Rule rule) { if (rule != null) rules.add(rule); }
    public synchronized List<Rule> getRules() {
        return Collections.unmodifiableList(new ArrayList<Rule>(rules));
    }
    public synchronized void clear() { rules.clear(); }
}