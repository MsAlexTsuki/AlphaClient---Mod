package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores supplied inventory interaction records; no inventory state is read or modified. */
public final class InventoryInteractionAnalyzer {
    public static final class Action {
        private final long timestampMillis;
        private final String actionType;
        private final int slot;
        private final String itemDescription;

        public Action(long timestampMillis, String actionType, int slot, String itemDescription) {
            this.timestampMillis = timestampMillis;
            this.actionType = actionType == null ? "" : actionType.trim();
            this.slot = slot;
            this.itemDescription = itemDescription == null ? "" : itemDescription.trim();
        }
        public long getTimestampMillis() { return timestampMillis; }
        public String getActionType() { return actionType; }
        public int getSlot() { return slot; }
        public String getItemDescription() { return itemDescription; }
    }

    private final List<Action> actions = new ArrayList<Action>();
    public synchronized void add(Action action) { if (action != null) actions.add(action); }
    public synchronized List<Action> getActions() {
        return Collections.unmodifiableList(new ArrayList<Action>(actions));
    }
    public synchronized void clear() { actions.clear(); }
}