package com.alpharip.staffmod.achievements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Builds filtered and sorted wall snapshots from the existing local checklist. */
public final class AchievementWallService {
    private final AchievementChecklist checklist;
    private final AchievementWallSettings settings;

    public AchievementWallService(AchievementChecklist checklist, AchievementWallSettings settings) {
        this.checklist = checklist == null ? new AchievementChecklist() : checklist;
        this.settings = settings == null ? new AchievementWallSettings() : settings;
    }

    public AchievementWallSnapshot createSnapshot() {
        List<AchievementEntry> source = checklist.getEntries();
        List<AchievementWallEntry> visible = new ArrayList<AchievementWallEntry>();
        int unlocked = 0, locked = 0;
        String query = settings.getSearchQuery().toLowerCase(Locale.ROOT);
        for (AchievementEntry item : source) {
            if (item.isUnlocked()) unlocked++; else locked++;
            if (!settings.isEnabled()) continue;
            if (!settings.isShowLocked() && !item.isUnlocked()) continue;
            if (matches(item, query)) visible.add(new AchievementWallEntry(item));
        }
        sort(visible, settings.getSort());
        return new AchievementWallSnapshot(visible, source.size(), unlocked, locked);
    }

    private boolean matches(AchievementEntry entry, String query) {
        if (query.isEmpty()) return true;
        return contains(entry.getTitle(), query) || contains(entry.getDescription(), query)
                || contains(entry.getCategory(), query) || contains(entry.getExplanation(), query)
                || contains(entry.getId(), query);
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private void sort(List<AchievementWallEntry> entries, AchievementWallSort requested) {
        final AchievementWallSort mode = requested == null ? AchievementWallSort.RECENT_FIRST : requested;
        Collections.sort(entries, new Comparator<AchievementWallEntry>() {
            @Override public int compare(AchievementWallEntry a, AchievementWallEntry b) {
                if (mode == AchievementWallSort.TITLE)
                    return a.getTitle().compareToIgnoreCase(b.getTitle());
                if (mode == AchievementWallSort.CATEGORY) {
                    int category = a.getCategory().compareToIgnoreCase(b.getCategory());
                    return category != 0 ? category : a.getTitle().compareToIgnoreCase(b.getTitle());
                }
                int time = Long.compare(a.getUnlockedAtMillis(), b.getUnlockedAtMillis());
                if (mode == AchievementWallSort.RECENT_FIRST) time = -time;
                return time != 0 ? time : a.getTitle().compareToIgnoreCase(b.getTitle());
            }
        });
    }
}
