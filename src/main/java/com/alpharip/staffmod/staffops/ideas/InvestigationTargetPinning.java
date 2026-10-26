package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** In-memory set of pinned investigation targets. */
public final class InvestigationTargetPinning {
    private final Set<String> pinned = new LinkedHashSet<String>();

    public synchronized boolean pin(String targetId) {
        if (targetId == null || targetId.trim().isEmpty()) return false;
        return pinned.add(targetId);
    }

    public synchronized boolean unpin(String targetId) { return pinned.remove(targetId); }
    public synchronized boolean isPinned(String targetId) { return pinned.contains(targetId); }
    public synchronized List<String> getPinnedTargets() {
        return Collections.unmodifiableList(new ArrayList<String>(pinned));
    }
}