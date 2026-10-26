package com.alpharip.staffmod.staffops.ideas;

import java.util.HashSet;
import java.util.Set;

/** Tracks alert keys that are currently suppressed. */
public class DetectionAlertSuppression {
    private final Set<String> suppressed = new HashSet<String>();
    public boolean suppress(String alertKey) {
        return alertKey != null && !alertKey.trim().isEmpty() && suppressed.add(alertKey.trim());
    }
    public boolean isSuppressed(String alertKey) {
        return alertKey != null && suppressed.contains(alertKey.trim());
    }
    public boolean unsuppress(String alertKey) {
        return alertKey != null && suppressed.remove(alertKey.trim());
    }
}