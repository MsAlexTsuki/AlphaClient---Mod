package com.alpharip.staffmod.staffops.ideas;

/**
 * Local notification model for a staff leave event observed by the client.
 * It does not provide a guaranteed server-wide staff presence feed.
 */
public final class StaffLeaveAlert {
    private String staffName = "";
    private String message = "";
    private long timestampMillis;
    private boolean unread;

    public void publish(String staffName, String message, long timestampMillis) {
        this.staffName = staffName == null ? "" : staffName;
        this.message = message == null ? "" : message;
        this.timestampMillis = timestampMillis;
        this.unread = true;
    }

    public void markRead() { unread = false; }
    public String getStaffName() { return staffName; }
    public String getMessage() { return message; }
    public long getTimestampMillis() { return timestampMillis; }
    public boolean isUnread() { return unread; }

    // TODO(Supabase): define an authorized source if cross-client staff leave alerts are required.
}