package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores local dashboard widget definitions and their display order. */
public final class InvestigationDashboardWidgets {
    public static final class Widget {
        private final String id;
        private final String title;
        private final boolean enabled;

        public Widget(String id, String title, boolean enabled) {
            this.id = id == null ? "" : id.trim();
            this.title = title == null ? "" : title.trim();
            this.enabled = enabled;
        }
        public String getId() { return id; }
        public String getTitle() { return title; }
        public boolean isEnabled() { return enabled; }
    }

    private final List<Widget> widgets = new ArrayList<Widget>();
    public synchronized void add(Widget widget) { if (widget != null) widgets.add(widget); }
    public synchronized List<Widget> getWidgets() {
        return Collections.unmodifiableList(new ArrayList<Widget>(widgets));
    }
    public synchronized boolean removeById(String id) {
        return widgets.removeIf(widget -> widget.getId().equals(id));
    }
}