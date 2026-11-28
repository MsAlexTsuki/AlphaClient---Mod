package com.alpharip.staffdashboardexpansion.presence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Filtering, sorting and stale-observation policy for the local presence snapshot. */
public final class StaffPresenceManager {
    public enum StatusFilter { ALL, ONLINE, OFFLINE, UNKNOWN }
    public enum SortMode { NAME, LAST_SEEN_NEWEST, LAST_SEEN_OLDEST }

    public static final long DEFAULT_STALE_AFTER_MILLIS = 5L * 60L * 1000L;

    private final StaffPresenceList presenceList;
    private StatusFilter filter = StatusFilter.ALL;
    private SortMode sortMode = SortMode.LAST_SEEN_NEWEST;
    private long staleAfterMillis = DEFAULT_STALE_AFTER_MILLIS;

    public StaffPresenceManager() { this(new StaffPresenceList()); }

    public StaffPresenceManager(StaffPresenceList presenceList) {
        if (presenceList == null) throw new IllegalArgumentException("presenceList cannot be null");
        this.presenceList = presenceList;
    }

    public StaffPresenceList getPresenceList() { return presenceList; }
    public void update(String name, String status, long observedAtMillis) {
        presenceList.update(name, status, observedAtMillis);
    }
    public void remove(String name) { presenceList.remove(name); }
    public void clear() { presenceList.clear(); }

    public StatusFilter getFilter() { return filter; }
    public void setFilter(StatusFilter filter) {
        this.filter = filter == null ? StatusFilter.ALL : filter;
    }
    public SortMode getSortMode() { return sortMode; }
    public void setSortMode(SortMode sortMode) {
        this.sortMode = sortMode == null ? SortMode.LAST_SEEN_NEWEST : sortMode;
    }
    public long getStaleAfterMillis() { return staleAfterMillis; }
    public void setStaleAfterMillis(long value) {
        if (value < 0L) throw new IllegalArgumentException("stale threshold cannot be negative");
        staleAfterMillis = value;
    }

    public List<StaffPresenceEntry> getVisibleEntries(long nowMillis) {
        List<StaffPresenceEntry> result = new ArrayList<StaffPresenceEntry>();
        for (StaffPresenceEntry entry : presenceList.getEntries()) {
            if (matchesFilter(entry)) result.add(entry);
        }
        Comparator<StaffPresenceEntry> comparator;
        if (sortMode == SortMode.NAME) {
            comparator = new Comparator<StaffPresenceEntry>() {
                public int compare(StaffPresenceEntry a, StaffPresenceEntry b) {
                    return a.getStaffName().compareToIgnoreCase(b.getStaffName());
                }
            };
        } else {
            comparator = new Comparator<StaffPresenceEntry>() {
                public int compare(StaffPresenceEntry a, StaffPresenceEntry b) {
                    int byTime = Long.compare(a.getObservedAtMillis(), b.getObservedAtMillis());
                    return sortMode == SortMode.LAST_SEEN_OLDEST ? byTime : -byTime;
                }
            };
        }
        Collections.sort(result, comparator);
        return result;
    }

    public boolean isStale(StaffPresenceEntry entry, long nowMillis) {
        return entry == null || entry.isStale(nowMillis, staleAfterMillis);
    }

    public int count(StatusFilter status) {
        int count = 0;
        for (StaffPresenceEntry entry : presenceList.getEntries()) {
            if (status == StatusFilter.ALL || matches(entry, status)) count++;
        }
        return count;
    }

    private boolean matchesFilter(StaffPresenceEntry entry) {
        return filter == StatusFilter.ALL || matches(entry, filter);
    }

    private boolean matches(StaffPresenceEntry entry, StatusFilter wanted) {
        String value = entry.getStatus().toLowerCase(Locale.ROOT);
        if (wanted == StatusFilter.ONLINE) return value.equals("online");
        if (wanted == StatusFilter.OFFLINE) return value.equals("offline");
        return !(value.equals("online") || value.equals("offline"));
    }
}
