package com.alpharip.staffmod.staffops.ideas;

/** Local focus lock state for an investigation workspace. */
public final class InvestigationFocusLock {
    private String targetId;
    private boolean locked;

    public synchronized void lock(String id) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("target id is required");
        targetId = id;
        locked = true;
    }

    public synchronized void unlock() { locked = false; targetId = null; }
    public synchronized boolean isLocked() { return locked; }
    public synchronized String getTargetId() { return targetId; }
    public synchronized boolean allowsTarget(String id) { return !locked || (targetId != null && targetId.equals(id)); }
}