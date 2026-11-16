package com.alpharip.staffmod.caseescalationtimer;

import java.io.File;
import java.util.List;

/** Standalone entry point for the Case Escalation Timer prototype. */
public final class CaseEscalationTimer {
    private final EscalationTracker tracker;
    private final EscalationPolicy policy;
    private final EscalationNotificationManager notifications;
    private final EscalationStorage storage;

    public CaseEscalationTimer(File dataFile, EscalationPolicy policy) {
        this.policy = policy == null ? EscalationPolicy.withoutWarnings() : policy;
        this.tracker = new EscalationTracker();
        this.notifications = new EscalationNotificationManager();
        this.storage = new EscalationStorage(dataFile);
    }

    public void load() {
        List<EscalationCase> loaded = storage.load();
        tracker.replaceAll(loaded);
        notifications.reset();
    }

    public boolean save() { return storage.save(tracker.getCases()); }
    public EscalationTracker getTracker() { return tracker; }
    public EscalationPolicy getPolicy() { return policy; }
    public List<EscalationNotification> collectNotifications(long now) {
        return notifications.collect(tracker.getCases(), policy, now);
    }
    public CaseEscalationTimerGui createGui() { return new CaseEscalationTimerGui(null, this); }
}
