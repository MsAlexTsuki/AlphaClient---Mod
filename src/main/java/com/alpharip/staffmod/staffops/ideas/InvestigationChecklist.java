package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Local checklist for investigation workflow preparation. */
public final class InvestigationChecklist {
    private final List<Item> items = new ArrayList<Item>();

    public Item add(String label) {
        if (label == null || label.trim().isEmpty()) {
            throw new IllegalArgumentException("Checklist label must not be blank");
        }
        Item item = new Item(label.trim());
        items.add(item);
        return item;
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(new ArrayList<Item>(items));
    }

    public static final class Item {
        private final String label;
        private boolean complete;

        private Item(String label) { this.label = label; }
        public String getLabel() { return label; }
        public boolean isComplete() { return complete; }
        public void setComplete(boolean complete) { this.complete = complete; }
    }
}