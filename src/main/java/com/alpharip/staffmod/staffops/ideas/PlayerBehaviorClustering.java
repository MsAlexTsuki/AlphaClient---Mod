package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Groups player identifiers by caller-defined cluster. */
public class PlayerBehaviorClustering {
    private final Map<String, Set<String>> clusters = new HashMap<String, Set<String>>();
    public boolean assign(String cluster, String playerId) {
        if (blank(cluster) || blank(playerId)) { return false; }
        Set<String> players = clusters.get(cluster.trim());
        if (players == null) { players = new HashSet<String>(); clusters.put(cluster.trim(), players); }
        players.add(playerId.trim());
        return true;
    }
    public Set<String> getPlayers(String cluster) {
        Set<String> players = cluster == null ? null : clusters.get(cluster.trim());
        return players == null ? Collections.<String>emptySet()
                : Collections.unmodifiableSet(new HashSet<String>(players));
    }
    private boolean blank(String value) { return value == null || value.trim().isEmpty(); }
}