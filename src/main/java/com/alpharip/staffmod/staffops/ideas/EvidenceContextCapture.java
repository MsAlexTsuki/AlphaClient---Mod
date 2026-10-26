package com.alpharip.staffmod.staffops.ideas;


import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Captures context values supplied by the caller; it does not inspect or upload game state. */
public final class EvidenceContextCapture {
    private final Map<String, String> context = new LinkedHashMap<String, String>();
    private long capturedAtMillis;

    public void capture(Map<String, String> values, long capturedAtMillis) {
        context.clear();
        if (values != null) {
            for (Map.Entry<String, String> entry : values.entrySet()) {
                if (entry.getKey() != null) {
                    context.put(entry.getKey(), entry.getValue() == null ? "" : entry.getValue());
                }
            }
        }
        this.capturedAtMillis = capturedAtMillis;
    }

    public Map<String, String> getContext() {
        return Collections.unmodifiableMap(new LinkedHashMap<String, String>(context));
    }

    public long getCapturedAtMillis() { return capturedAtMillis; }
    // TODO(Supabase): define approved fields and access controls before sharing captured context.
}