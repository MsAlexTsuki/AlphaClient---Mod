package com.alpharip.staffmod.autosave;

/** Thread-safe snapshot of the most recently observed singleplayer world-save event. */
public final class AutoSaveState {
    private long lastSaveAtMillis;
    private String lastWorldName = "";

    public synchronized void recordSave(String worldName, long timestampMillis) {
        lastWorldName = worldName == null ? "" : worldName;
        lastSaveAtMillis = Math.max(0L, timestampMillis);
    }

    public synchronized long getLastSaveAtMillis() { return lastSaveAtMillis; }
    public synchronized String getLastWorldName() { return lastWorldName; }

    public synchronized boolean hasRecentSave(long nowMillis, int windowSeconds) {
        if (lastSaveAtMillis <= 0L || nowMillis < lastSaveAtMillis) return false;
        return nowMillis - lastSaveAtMillis <= Math.max(1, windowSeconds) * 1000L;
    }

    public synchronized long getSecondsSinceSave(long nowMillis) {
        if (lastSaveAtMillis <= 0L || nowMillis < lastSaveAtMillis) return Long.MAX_VALUE;
        return (nowMillis - lastSaveAtMillis) / 1000L;
    }

    public synchronized void clear() {
        lastSaveAtMillis = 0L;
        lastWorldName = "";
    }
}
