package com.alpharip.staffmod.staffops.ideas;

import java.util.HashSet;
import java.util.Set;

/** Calculates simple similarity between two supplied label sets. */
public class BehaviorSimilarityEngine {
    public double similarity(Set<String> first, Set<String> second) {
        if (first == null || second == null) { return 0.0D; }
        Set<String> union = new HashSet<String>(first);
        union.addAll(second);
        if (union.isEmpty()) { return 1.0D; }
        Set<String> intersection = new HashSet<String>(first);
        intersection.retainAll(second);
        return (double) intersection.size() / union.size();
    }
}