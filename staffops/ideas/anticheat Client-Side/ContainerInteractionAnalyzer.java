package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores supplied container interaction records; it does not access open containers. */
public final class ContainerInteractionAnalyzer {
    public static final class Action {
        private final long timestampMillis;
        private final String containerType;
        private final String actionType;
        private final int slot;

        public Action(long timestampMillis, String containerType, String actionType, int slot) {
            this.timestampMillis = timestampMillis;
            this.containerType = containerType == null ? "" : containerType.trim();
            this.actionType = actionType == null ? "" : actionType.trim();
            this.slot = slot;
        }
        public long getTimestampMillis() { return timestampMillis; }
        public String getContainerType() { return containerType; }
        public String getActionType() { return actionType; }
        public int getSlot() { return slot; }
    }

    private final List<Action> actions = new ArrayList<Action>();
    public synchronized void add(Action action) { if (action != null) actions.add(action); }
    public synchronized List<Action> getActions() {
        return Collections.unmodifiableList(new ArrayList<Action>(actions));
    }
    public synchronized void clear() { actions.clear(); }
}