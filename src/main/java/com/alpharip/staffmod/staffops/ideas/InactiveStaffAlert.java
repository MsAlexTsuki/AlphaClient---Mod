package com.alpharip.staffmod.staffops.ideas;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Flags staff entries whose recorded last-seen time exceeds a configurable threshold.
 *
 * Activity timestamps must come from a trusted source before these alerts are
 * used operationally.
 * TODO(Supabase): obtain authenticated presence/last-seen data and apply
 * privacy and staff-permission policies.
 * Compatible with Java 8.
 */
public class InactiveStaffAlert {
    public static final class Alert {
        private final String staffId;
        private final LocalDateTime lastSeen;
        private final Duration inactiveFor;

        private Alert(String staffId, LocalDateTime lastSeen, Duration inactiveFor) {
            this.staffId = staffId;
            this.lastSeen = lastSeen;
            this.inactiveFor = inactiveFor;
        }

        public String getStaffId() { return staffId; }
        public LocalDateTime getLastSeen() { return lastSeen; }
        public Duration getInactiveFor() { return inactiveFor; }
    }

    private final Map<String, LocalDateTime> lastSeenByStaff =
            new LinkedHashMap<String, LocalDateTime>();
    private long thresholdMinutes = 30L;

    public void setThresholdMinutes(long thresholdMinutes) {
        if (thresholdMinutes < 1L) {
            throw new IllegalArgumentException("thresholdMinutes must be at least 1.");
        }
        this.thresholdMinutes = thresholdMinutes;
    }

    public long getThresholdMinutes() {
        return thresholdMinutes;
    }

    public void recordLastSeen(String staffId, LocalDateTime lastSeen) {
        String id = requireText(staffId, "staffId");
        if (lastSeen == null) {
            throw new IllegalArgumentException("lastSeen must not be null.");
        }
        lastSeenByStaff.put(id, lastSeen);
    }

    public List<Alert> getAlerts(LocalDateTime now) {
        if (now == null) {
            throw new IllegalArgumentException("now must not be null.");
        }
        List<Alert> alerts = new ArrayList<Alert>();
        Duration threshold = Duration.ofMinutes(thresholdMinutes);
        for (Map.Entry<String, LocalDateTime> entry : lastSeenByStaff.entrySet()) {
            LocalDateTime lastSeen = entry.getValue();
            if (!lastSeen.isAfter(now)) {
                Duration inactiveFor = Duration.between(lastSeen, now);
                if (inactiveFor.compareTo(threshold) >= 0) {
                    alerts.add(new Alert(entry.getKey(), lastSeen, inactiveFor));
                }
            }
        }
        return Collections.unmodifiableList(alerts);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
        return value.trim();
    }
}
