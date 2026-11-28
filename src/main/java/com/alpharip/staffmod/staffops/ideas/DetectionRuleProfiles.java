package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Stores named groups of detection rules. */
public class DetectionRuleProfiles {
    private final Map<String, List<String>> profiles = new HashMap<String, List<String>>();
    public boolean addRule(String profile, String rule) {
        if (blank(profile) || blank(rule)) { return false; }
        String key = profile.trim();
        List<String> rules = profiles.get(key);
        if (rules == null) { rules = new ArrayList<String>(); profiles.put(key, rules); }
        rules.add(rule.trim());
        return true;
    }
    public List<String> getRules(String profile) {
        List<String> rules = profile == null ? null : profiles.get(profile.trim());
        return rules == null ? Collections.<String>emptyList()
                : Collections.unmodifiableList(new ArrayList<String>(rules));
    }
    private boolean blank(String value) { return value == null || value.trim().isEmpty(); }
}