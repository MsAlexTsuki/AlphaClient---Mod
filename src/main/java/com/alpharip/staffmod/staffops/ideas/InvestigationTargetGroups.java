package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Local named groups of investigation target identifiers. */
public final class InvestigationTargetGroups {
    private final Map<String, Set<String>> groups = new LinkedHashMap<String, Set<String>>();

    public synchronized void add(String groupName, String targetId) {
        if (groupName == null || groupName.trim().isEmpty() || targetId == null || targetId.trim().isEmpty())
            throw new IllegalArgumentException("group name and target id are required");
        Set<String> targets = groups.get(groupName);
        if (targets == null) { targets = new LinkedHashSet<String>(); groups.put(groupName, targets); }
        targets.add(targetId);
    }

    public synchronized boolean remove(String groupName, String targetId) {
        Set<String> targets = groups.get(groupName);
        return targets != null && targets.remove(targetId);
    }

    public synchronized List<String> getGroupNames() {
        return Collections.unmodifiableList(new ArrayList<String>(groups.keySet()));
    }

    public synchronized List<String> getTargets(String groupName) {
        Set<String> targets = groups.get(groupName);
        return targets == null ? Collections.<String>emptyList()
            : Collections.unmodifiableList(new ArrayList<String>(targets));
    }
}