package com.alpharip.staffmod.AccessReviewReminder;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Manages individual staff access-review schedules and local persistence. */
public final class AccessReviewReminder {
    public static final String NAME = "Access Review Reminder";
    public static final String CATEGORY = "OwnerStaffing";
    public static final String STATUS = "NEW";
    public static final String REQUIRED_ACCESS = "Unconfirmed";

    private static final AccessReviewReminder INSTANCE = new AccessReviewReminder();
    private final List<AccessReviewEntry> entries = new ArrayList<AccessReviewEntry>();
    private boolean loaded;

    private AccessReviewReminder() { }

    public static AccessReviewReminder getInstance() {
        return INSTANCE;
    }

    public synchronized void load() {
        if (loaded) return;
        entries.clear();
        entries.addAll(AccessReviewStorage.load(getDataFile()));
        loaded = true;
    }

    public synchronized List<AccessReviewEntry> getEntries() {
        ensureLoaded();
        return Collections.unmodifiableList(new ArrayList<AccessReviewEntry>(entries));
    }

    public synchronized void addStaff(String staffName, String rank, int intervalDays) {
        ensureLoaded();
        String name = normalize(staffName);
        validate(name, intervalDays);
        for (AccessReviewEntry entry : entries) {
            if (entry.getStaffName().equalsIgnoreCase(name)) {
                throw new IllegalArgumentException("This staff member is already being tracked.");
            }
        }
        entries.add(new AccessReviewEntry(UUID.randomUUID().toString(), name,
                normalize(rank), 0L, intervalDays));
        save();
    }

    public synchronized void updateStaff(String id, String staffName, String rank, int intervalDays) {
        ensureLoaded();
        String name = normalize(staffName);
        validate(name, intervalDays);
        AccessReviewEntry selected = find(id);
        if (selected == null) throw new IllegalArgumentException("Staff member not found.");
        for (AccessReviewEntry entry : entries) {
            if (entry != selected && entry.getStaffName().equalsIgnoreCase(name)) {
                throw new IllegalArgumentException("This staff member is already being tracked.");
            }
        }
        selected.update(name, normalize(rank), intervalDays);
        save();
    }

    public synchronized void markReviewed(String id) {
        ensureLoaded();
        AccessReviewEntry entry = find(id);
        if (entry == null) throw new IllegalArgumentException("Staff member not found.");
        entry.markReviewed(System.currentTimeMillis());
        save();
    }

    public synchronized void removeStaff(String id) {
        ensureLoaded();
        AccessReviewEntry entry = find(id);
        if (entry != null) {
            entries.remove(entry);
            save();
        }
    }

    public synchronized int countDue(long now) {
        ensureLoaded();
        int count = 0;
        for (AccessReviewEntry entry : entries) {
            if (entry.getStatus(now) == AccessReviewEntry.ReviewStatus.DUE) count++;
        }
        return count;
    }

    public synchronized int countUpcoming(long now) {
        ensureLoaded();
        int count = 0;
        for (AccessReviewEntry entry : entries) {
            if (entry.getStatus(now) == AccessReviewEntry.ReviewStatus.UPCOMING) count++;
        }
        return count;
    }

    private AccessReviewEntry find(String id) {
        for (AccessReviewEntry entry : entries) {
            if (entry.getId().equals(id)) return entry;
        }
        return null;
    }

    private void ensureLoaded() {
        if (!loaded) load();
    }

    private void save() {
        if (!AccessReviewStorage.save(getDataFile(), entries)) {
            System.out.println("[AlphaClient] Access Review Reminder data could not be saved.");
        }
    }

    private static void validate(String name, int intervalDays) {
        if (name.isEmpty()) throw new IllegalArgumentException("Enter a staff member name.");
        if (intervalDays < 1 || intervalDays > 3650) {
            throw new IllegalArgumentException("Review interval must be between 1 and 3650 days.");
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static File getDataFile() {
        Minecraft minecraft = Minecraft.getMinecraft();
        File base = minecraft == null ? new File(".") : minecraft.mcDataDir;
        return new File(new File(base, "config"), "alphaclient-access-review-reminder.json");
    }
}