package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** In-memory target history used by a future investigation quick-switch UI. */
public final class InvestigationQuickSwitch {
    private final List<String> targets = new ArrayList<String>();

    public synchronized void remember(String targetId) {
        if (targetId == null || targetId.trim().isEmpty()) return;
        targets.remove(targetId);
        targets.add(0, targetId);
    }

    public synchronized List<String> getTargets() {
        return Collections.unmodifiableList(new ArrayList<String>(targets));
    }

    public synchronized String getNext(String currentId) {
        if (targets.isEmpty()) return null;
        int index = targets.indexOf(currentId);
        return targets.get((index + 1 + targets.size()) % targets.size());
    }
}