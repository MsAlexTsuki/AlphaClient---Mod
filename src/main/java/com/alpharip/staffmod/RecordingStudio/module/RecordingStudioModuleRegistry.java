package com.alpharip.staffmod.RecordingStudio.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Thread-safe registry for optional Recording Studio module integrations. */
public final class RecordingStudioModuleRegistry {
    private static final Map<String, RecordingStudioModule> MODULES =
            new LinkedHashMap<String, RecordingStudioModule>();

    private RecordingStudioModuleRegistry() { }

    public static synchronized void register(RecordingStudioModule module) {
        if (module == null || module.getId() == null || module.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("A module with a stable ID is required");
        }
        MODULES.put(module.getId(), module);
    }

    public static synchronized void unregister(RecordingStudioModule module) {
        if (module == null) return;
        if (MODULES.get(module.getId()) == module) MODULES.remove(module.getId());
    }

    public static synchronized RecordingStudioModule get(String id) {
        return MODULES.get(id);
    }

    public static synchronized List<RecordingStudioModule> getAll() {
        return Collections.unmodifiableList(
                new ArrayList<RecordingStudioModule>(MODULES.values()));
    }
}
