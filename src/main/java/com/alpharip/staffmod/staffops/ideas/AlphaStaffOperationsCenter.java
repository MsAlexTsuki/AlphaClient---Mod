package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data model for a future Alpha Staff Operations Center UI.
 * It is a local dashboard shell only; it does not execute staff actions.
 */
public final class AlphaStaffOperationsCenter {
    private final List<PanelStatus> panels = new ArrayList<PanelStatus>();

    public void setPanelStatus(String panelId, String label, String status) {
        if (panelId == null || panelId.trim().isEmpty()) {
            throw new IllegalArgumentException("panelId must not be blank");
        }
        for (int i = 0; i < panels.size(); i++) {
            if (panelId.equals(panels.get(i).getPanelId())) {
                panels.set(i, new PanelStatus(panelId, label, status));
                return;
            }
        }
        panels.add(new PanelStatus(panelId, label, status));
    }

    public List<PanelStatus> getPanels() {
        return Collections.unmodifiableList(new ArrayList<PanelStatus>(panels));
    }

    public static final class PanelStatus {
        private final String panelId;
        private final String label;
        private final String status;

        public PanelStatus(String panelId, String label, String status) {
            this.panelId = panelId;
            this.label = label == null ? "" : label;
            this.status = status == null ? "Unavailable" : status;
        }

        public String getPanelId() { return panelId; }
        public String getLabel() { return label; }
        public String getStatus() { return status; }
    }

    // TODO(Supabase): fetch authorized operational data only after access rules are defined.
}