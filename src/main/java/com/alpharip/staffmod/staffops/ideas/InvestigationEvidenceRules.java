package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Local evidence-handling rule definitions; does not validate or enforce evidence policy. */
public final class InvestigationEvidenceRules {
    public static final class Rule {
        private final String condition;
        private final String requiredAction;
        private final boolean mandatory;

        public Rule(String condition, String requiredAction, boolean mandatory) {
            this.condition = condition == null ? "" : condition.trim();
            this.requiredAction = requiredAction == null ? "" : requiredAction.trim();
            this.mandatory = mandatory;
        }
        public String getCondition() { return condition; }
        public String getRequiredAction() { return requiredAction; }
        public boolean isMandatory() { return mandatory; }
    }

    private final List<Rule> rules = new ArrayList<Rule>();

    public synchronized void add(Rule rule) { if (rule != null) rules.add(rule); }
    public synchronized List<Rule> getRules() {
        return Collections.unmodifiableList(new ArrayList<Rule>(rules));
    }
    public synchronized void clear() { rules.clear(); }
}