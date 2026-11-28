package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Collects alert records provided by another detection component. */
public class DetectionAlertCenter {
    public static final class Alert {
        public final long timestampMillis;
        public final String level;
        public final String message;

        public Alert(long timestampMillis, String level, String message) {
            this.timestampMillis = timestampMillis;
            this.level = level == null ? "" : level;
            this.message = message == null ? "" : message;
        }
    }

    private final List<Alert> alerts = new ArrayList<Alert>();

    public void add(long timestampMillis, String level, String message) {
        alerts.add(new Alert(timestampMillis, level, message));
    }

    public List<Alert> getAlerts() {
        return Collections.unmodifiableList(new ArrayList<Alert>(alerts));
    }

    public void clear() { alerts.clear(); }
}
