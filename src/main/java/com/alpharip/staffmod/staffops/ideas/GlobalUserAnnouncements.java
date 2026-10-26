package com.alpharip.staffmod.staffops.ideas;

import java.time.Instant;

/**
 * Foundation for global announcements shown to users.
 * TODO(Supabase): store announcements and delivery/read state.
 * Delivery channels, authorization, UI integration, and tests remain to be implemented.
 */
public class GlobalUserAnnouncements {
    private String id;
    private String title;
    private String message;
    private String createdBy;
    private Instant createdAt;
    private boolean enabled = true;

    public GlobalUserAnnouncements(String id, String title, String message, String createdBy, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
}