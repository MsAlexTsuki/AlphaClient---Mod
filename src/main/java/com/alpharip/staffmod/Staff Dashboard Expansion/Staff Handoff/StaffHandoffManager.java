package com.alpharip.staffmod.staffdashboardexpansion.staffhandoff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Standalone in-memory handoff manager.
 *
 * It does not resolve staff identities, grant permissions, send notifications,
 * or synchronize data between clients.
 */
public final class StaffHandoffManager {
    private final List<StaffHandoff> handoffs = new ArrayList<StaffHandoff>();
    private long nextId = 1L;
    private int maximumHandoffs = 100;

    public synchronized StaffHandoff create(String fromStaff, String toStaff, String summary) {
        String sender = requireText(fromStaff, "fromStaff");
        String recipient = requireText(toStaff, "toStaff");
        String details = requireText(summary, "summary");
        if (sender.equalsIgnoreCase(recipient)) {
            throw new IllegalArgumentException("A handoff recipient must be another staff member.");
        }
        if (handoffs.size() >= maximumHandoffs) {
            throw new IllegalStateException("The handoff list has reached its capacity.");
        }

        StaffHandoff handoff = new StaffHandoff(
            nextId++, sender, recipient, details, System.currentTimeMillis());
        handoffs.add(handoff);
        return handoff;
    }

    public synchronized boolean accept(long id, String actingStaff) {
        StaffHandoff handoff = find(id);
        if (!canRecipientRespond(handoff, actingStaff)) {
            return false;
        }
        handoff.setStatus(StaffHandoffStatus.ACCEPTED);
        return true;
    }

    public synchronized boolean reject(long id, String actingStaff) {
        StaffHandoff handoff = find(id);
        if (!canRecipientRespond(handoff, actingStaff)) {
            return false;
        }
        handoff.setStatus(StaffHandoffStatus.REJECTED);
        return true;
    }

    public synchronized boolean withdraw(long id, String actingStaff) {
        StaffHandoff handoff = find(id);
        if (handoff == null || handoff.getStatus() != StaffHandoffStatus.PENDING
                || !handoff.getFromStaff().equalsIgnoreCase(safeText(actingStaff))) {
            return false;
        }
        handoff.setStatus(StaffHandoffStatus.WITHDRAWN);
        return true;
    }

    public synchronized StaffHandoff get(long id) {
        return find(id);
    }

    public synchronized List<StaffHandoff> getAll() {
        List<StaffHandoff> result = new ArrayList<StaffHandoff>(handoffs);
        Collections.sort(result, new Comparator<StaffHandoff>() {
            @Override
            public int compare(StaffHandoff left, StaffHandoff right) {
                return Long.compare(right.getCreatedAtMillis(), left.getCreatedAtMillis());
            }
        });
        return Collections.unmodifiableList(result);
    }

    public synchronized List<StaffHandoff> getForRecipient(String staffName) {
        String recipient = requireText(staffName, "staffName");
        List<StaffHandoff> result = new ArrayList<StaffHandoff>();
        for (StaffHandoff handoff : getAll()) {
            if (handoff.getToStaff().equalsIgnoreCase(recipient)) {
                result.add(handoff);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized List<StaffHandoff> getByStatus(StaffHandoffStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null.");
        }
        List<StaffHandoff> result = new ArrayList<StaffHandoff>();
        for (StaffHandoff handoff : getAll()) {
            if (handoff.getStatus() == status) {
                result.add(handoff);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized int getPendingCount() {
        int count = 0;
        for (StaffHandoff handoff : handoffs) {
            if (handoff.getStatus() == StaffHandoffStatus.PENDING) {
                count++;
            }
        }
        return count;
    }

    public synchronized boolean remove(long id) {
        StaffHandoff handoff = find(id);
        return handoff != null && handoffs.remove(handoff);
    }

    public synchronized void setMaximumHandoffs(int maximumHandoffs) {
        if (maximumHandoffs < 1 || maximumHandoffs < handoffs.size()) {
            throw new IllegalArgumentException("Capacity must be positive and not below the current count.");
        }
        this.maximumHandoffs = maximumHandoffs;
    }

    private boolean canRecipientRespond(StaffHandoff handoff, String actingStaff) {
        return handoff != null
            && handoff.getStatus() == StaffHandoffStatus.PENDING
            && handoff.getToStaff().equalsIgnoreCase(safeText(actingStaff));
    }

    private StaffHandoff find(long id) {
        for (StaffHandoff handoff : handoffs) {
            if (handoff.getId() == id) {
                return handoff;
            }
        }
        return null;
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
        return value.trim();
    }

    private static String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    // TODO(Supabase): replace local records with server-authorized shared handoffs.
}
