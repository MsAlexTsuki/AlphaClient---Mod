package com.alpharip.staffmod.staffops.ideas;

import java.time.Instant;

/**
 * Foundation for maintenance-mode notices.
 * TODO(Supabase): persist maintenance state and notification scheduling.
 * Launcher/client delivery, access control, UI, and tests remain to be implemented.
 */
public class MaintenanceModeNotifications {
    private boolean maintenanceActive;
    private String message;
    private Instant scheduledStart;
    private Instant scheduledEnd;

    public boolean isMaintenanceActive() { return maintenanceActive; }
    public void setMaintenanceActive(boolean value) { maintenanceActive = value; }
    public String getMessage() { return message; }
    public void setMessage(String value) { message = value; }
    public Instant getScheduledStart() { return scheduledStart; }
    public void setScheduledStart(Instant value) { scheduledStart = value; }
    public Instant getScheduledEnd() { return scheduledEnd; }
    public void setScheduledEnd(Instant value) { scheduledEnd = value; }
}