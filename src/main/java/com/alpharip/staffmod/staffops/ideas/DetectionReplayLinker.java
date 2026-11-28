package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Associates detection identifiers with replay references supplied by a caller. */
public class DetectionReplayLinker {
    private final Map<String, String> replayReferences = new HashMap<String, String>();

    public boolean link(String detectionId, String replayReference) {
        String id = normalize(detectionId);
        String reference = normalize(replayReference);
        if (id == null || reference == null) {
            return false;
        }
        replayReferences.put(id, reference);
        return true;
    }

    public String getReplayReference(String detectionId) {
        String id = normalize(detectionId);
        return id == null ? null : replayReferences.get(id);
    }

    public boolean unlink(String detectionId) {
        String id = normalize(detectionId);
        return id != null && replayReferences.remove(id) != null;
    }

    public Map<String, String> snapshot() {
        return Collections.unmodifiableMap(new HashMap<String, String>(replayReferences));
    }

    private String normalize(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
