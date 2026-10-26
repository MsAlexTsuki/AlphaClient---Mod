package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores user-defined investigation panel metadata without rendering a UI. */
public final class InvestigationCustomPanels {
    public static final class Panel {
        private final String id;
        private final String title;
        private final String contentKey;

        public Panel(String id, String title, String contentKey) {
            this.id = id == null ? "" : id.trim();
            this.title = title == null ? "" : title.trim();
            this.contentKey = contentKey == null ? "" : contentKey.trim();
        }
        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getContentKey() { return contentKey; }
    }

    private final List<Panel> panels = new ArrayList<Panel>();
    public synchronized void add(Panel panel) { if (panel != null) panels.add(panel); }
    public synchronized List<Panel> getPanels() {
        return Collections.unmodifiableList(new ArrayList<Panel>(panels));
    }
}