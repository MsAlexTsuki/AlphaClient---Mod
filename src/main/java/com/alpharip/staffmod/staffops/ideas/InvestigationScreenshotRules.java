package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores screenshot capture criteria; no screenshot is taken automatically by this model. */
public final class InvestigationScreenshotRules {
    public static final class Rule {
        private final String trigger;
        private final String label;
        private final boolean enabled;

        public Rule(String trigger, String label, boolean enabled) {
            this.trigger = trigger == null ? "" : trigger.trim();
            this.label = label == null ? "" : label.trim();
            this.enabled = enabled;
        }
        public String getTrigger() { return trigger; }
        public String getLabel() { return label; }
        public boolean isEnabled() { return enabled; }
    }

    private final List<Rule> rules = new ArrayList<Rule>();
    public synchronized void add(Rule rule) { if (rule != null) rules.add(rule); }
    public synchronized List<Rule> getRules() {
        return Collections.unmodifiableList(new ArrayList<Rule>(rules));
    }
    public synchronized void clear() { rules.clear(); }
}