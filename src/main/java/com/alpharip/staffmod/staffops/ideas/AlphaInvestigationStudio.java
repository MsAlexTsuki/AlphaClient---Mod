package com.alpharip.staffmod.staffops.ideas;

import java.util.LinkedHashMap;
import java.util.Map;

/** Local composition root for investigation feature models; no UI or remote services are wired here. */
public final class AlphaInvestigationStudio {
    private final Map<String, Object> modules = new LinkedHashMap<String, Object>();

    public AlphaInvestigationStudio() {
        register("timeline", new InvestigationTimelineBuilder());
        register("eventFilter", new InvestigationEventFilter());
        register("eventSearch", new InvestigationEventSearch());
        register("workflow", new InvestigationWorkflowBuilder());
        register("changeHistory", new InvestigationChangeHistory());
        register("collaborationNotes", new InvestigationCollaborationNotes());
        register("notifications", new InvestigationNotifications());
    }

    public synchronized void register(String key, Object module) {
        if (key != null && !key.trim().isEmpty() && module != null) modules.put(key.trim(), module);
    }
    public synchronized Object getModule(String key) { return modules.get(key); }
    public synchronized Map<String, Object> getModules() {
        return java.util.Collections.unmodifiableMap(new LinkedHashMap<String, Object>(modules));
    }
}