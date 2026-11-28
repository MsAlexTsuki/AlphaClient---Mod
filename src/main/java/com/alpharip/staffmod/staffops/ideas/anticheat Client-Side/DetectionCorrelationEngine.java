package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Groups caller-supplied signal labels by a shared correlation key. */
public class DetectionCorrelationEngine {
    private final Map<String, List<String>> signalsByKey =
            new HashMap<String, List<String>>();

    public boolean addSignal(String correlationKey, String signal) {
        String key = normalize(correlationKey);
        String value = normalize(signal);
        if (key == null || value == null) {
            return false;
        }
        List<String> signals = signalsByKey.get(key);
        if (signals == null) {
            signals = new ArrayList<String>();
            signalsByKey.put(key, signals);
        }
        signals.add(value);
        return true;
    }

    public List<String> getSignals(String correlationKey) {
        String key = normalize(correlationKey);
        List<String> signals = key == null ? null : signalsByKey.get(key);
        return signals == null ? Collections.<String>emptyList()
                : Collections.unmodifiableList(new ArrayList<String>(signals));
    }

    public void clearKey(String correlationKey) {
        String key = normalize(correlationKey);
        if (key != null) {
            signalsByKey.remove(key);
        }
    }

    private String normalize(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
