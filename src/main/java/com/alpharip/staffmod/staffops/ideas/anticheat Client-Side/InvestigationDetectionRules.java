package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores local investigation detection rule definitions; it does not detect activity itself. */
public final class InvestigationDetectionRules {
    public static final class Rule {
        private final String name;
        private final String signal;
        private final String response;

        public Rule(String name, String signal, String response) {
            this.name = name == null ? "" : name.trim();
            this.signal = signal == null ? "" : signal.trim();
            this.response = response == null ? "" : response.trim();
        }
        public String getName() { return name; }
        public String getSignal() { return signal; }
        public String getResponse() { return response; }
    }

    private final List<Rule> rules = new ArrayList<Rule>();
    public synchronized void add(Rule rule) { if (rule != null) rules.add(rule); }
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