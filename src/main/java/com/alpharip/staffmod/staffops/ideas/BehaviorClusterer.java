package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Groups behavior labels under names supplied by the caller. */
public class BehaviorClusterer {
    private final Map<String, List<String>> clusters =
            new HashMap<String, List<String>>();

    public boolean add(String clusterName, String label) {
        if (clusterName == null || clusterName.trim().isEmpty()
                || label == null || label.trim().isEmpty()) {
            return false;
        }
        String key = clusterName.trim();
        List<String> values = clusters.get(key);
        if (values == null) {
            values = new ArrayList<String>();
            clusters.put(key, values);
        }
        values.add(label.trim());
        return true;
    }

    public List<String> getBehaviors(String clusterName) {
        List<String> values = clusterName == null ? null : clusters.get(clusterName.trim());
        return values == null ? Collections.<String>emptyList()
                : Collections.unmodifiableList(new ArrayList<String>(values));
    }

    public void clear() {
        clusters.clear();
    }
}
