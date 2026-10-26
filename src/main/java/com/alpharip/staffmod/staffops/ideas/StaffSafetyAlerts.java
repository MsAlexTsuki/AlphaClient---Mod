package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In-memory alert queue for future client-side safety UI.
 * Alerts are manually supplied local messages, not live staff/server alerts.
 * TODO(Supabase): define an authenticated alert source, delivery model and
 * account/rank access rules before adding shared alerts.
 */
public final class StaffSafetyAlerts {
    public static final String FEATURE_NAME = "Staff Safety Alerts";
    private final List<String> alerts = new ArrayList<String>();

    public void addLocalAlert(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Alert message cannot be blank");
        }
        alerts.add(message.trim());
    }

    public List<String> getAlerts() {
        return Collections.unmodifiableList(new ArrayList<String>(alerts));
    }

    public void dismissLocalAlert(int index) {
        if (index < 0 || index >= alerts.size()) {
            throw new IndexOutOfBoundsException("No local alert at index " + index);
        }
        alerts.remove(index);
    }
}
