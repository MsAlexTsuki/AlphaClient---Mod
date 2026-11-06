package com.alpharip.staffmod.achievements.suffix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.alpharip.staffmod.achievements.AchievementChecklist;
import com.alpharip.staffmod.achievements.AchievementEntry;

/**
 * Resolves suffix availability against the existing local achievement checklist.
 * This service does not render a player name or connect to account/server data.
 */
public final class AchievementSuffixService {
    private final AchievementSuffixRegistry registry;
    private final AchievementSuffixSettings settings;

    public AchievementSuffixService(AchievementSuffixRegistry registry,
                                    AchievementSuffixSettings settings) {
        this.registry = registry == null ? new AchievementSuffixRegistry() : registry;
        this.settings = settings == null ? new AchievementSuffixSettings() : settings;
    }

    public AchievementSuffixRegistry getRegistry() { return registry; }
    public AchievementSuffixSettings getSettings() { return settings; }

    public boolean isUnlocked(AchievementSuffixEntry suffix, AchievementChecklist checklist) {
        if (suffix == null || checklist == null) return false;
        AchievementEntry requirement = checklist.get(suffix.getRequiredAchievementId());
        return requirement != null && requirement.isUnlocked();
    }

    public List<AchievementSuffixEntry> getUnlocked(AchievementChecklist checklist) {
        if (checklist == null) return Collections.emptyList();
        List<AchievementSuffixEntry> result = new ArrayList<AchievementSuffixEntry>();
        for (AchievementSuffixEntry entry : registry.getAll()) {
            if (isUnlocked(entry, checklist)) result.add(entry);
        }
        return Collections.unmodifiableList(result);
    }

    public boolean select(String suffixId, AchievementChecklist checklist) {
        AchievementSuffixEntry entry = registry.get(suffixId);
        if (!isUnlocked(entry, checklist)) return false;
        settings.setSelectedSuffixId(entry.getId());
        settings.setEnabled(true);
        return true;
    }

    public void disable() { settings.setEnabled(false); }

    public AchievementSuffixEntry getActiveSuffix(AchievementChecklist checklist) {
        if (!settings.isEnabled()) return null;
        AchievementSuffixEntry selected = registry.get(settings.getSelectedSuffixId());
        return isUnlocked(selected, checklist) ? selected : null;
    }
}
