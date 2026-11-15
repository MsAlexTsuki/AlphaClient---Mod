package com.alpharip.staffmod.AlphaCoins.alphacoinsplaytime;

import com.alpharip.staffmod.AlphaCoins.CORE.AlphaCoinsCore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Playtime reward service. Tracks active playtime per Alpha profile and credits rewards
 * through the shared AlphaCoins CORE wallet. This class is not registered automatically.
 */
public final class AlphaCoinsPlaytime {
    private static final long DAY_MILLIS = 24L * 60L * 60L * 1000L;

    private final File configDirectory;
    private final File dataDirectory;
    private final PlaytimeRewardStorage storage = new PlaytimeRewardStorage();
    private final AlphaCoinsCore coinsCore = AlphaCoinsCore.getInstance();
    private final Set<String> claimedMilestones = new LinkedHashSet<String>();

    private String activeProfileId;
    private File profileFile;
    private long totalPlaytimeMillis;
    private long dailyIntervalsAwarded;
    private long tenDayIntervalsAwarded;
    private boolean loaded;
    private boolean lastSaveSuccessful = true;

    public AlphaCoinsPlaytime(File configDirectory) {
        if (configDirectory == null) throw new IllegalArgumentException("Config directory is required.");
        this.configDirectory = configDirectory;
        this.dataDirectory = new File(configDirectory, "alphacoinsplaytime");
    }

    /** Loads one profile and activates the matching shared AlphaCoins CORE wallet. */
    public synchronized boolean activateProfile(String profileId) {
        if (profileId == null || profileId.trim().isEmpty()) return false;
        String normalized = profileId.trim();
        if (loaded && normalized.equals(activeProfileId)
                && normalized.equals(coinsCore.getActiveProfileId())) return true;

        save();
        coinsCore.activateProfile(normalized, configDirectory, null);
        if (!coinsCore.isLoaded()
                || !normalized.equals(coinsCore.getActiveProfileId())) return false;

        activeProfileId = normalized;
        profileFile = new File(dataDirectory, safeFileName(normalized) + ".properties");
        PlaytimeRewardStorage.Snapshot snapshot = storage.load(profileFile);
        totalPlaytimeMillis = snapshot.totalPlaytimeMillis;
        dailyIntervalsAwarded = snapshot.dailyIntervalsAwarded;
        tenDayIntervalsAwarded = snapshot.tenDayIntervalsAwarded;
        claimedMilestones.clear();
        claimedMilestones.addAll(snapshot.claimedMilestones);
        loaded = true;
        lastSaveSuccessful = saveProgress();
        if (lastSaveSuccessful) processDueRewards();
        return true;
    }

    /**
     * Adds elapsed time only when the caller confirms the player is actively in a world.
     * The event adapter supplies elapsed wall-clock time; this method never counts offline time.
     */
    public synchronized boolean recordActivePlaytime(long elapsedMillis) {
        if (!loaded || elapsedMillis <= 0L
                || !activeProfileId.equals(coinsCore.getActiveProfileId())) return false;
        if (Long.MAX_VALUE - totalPlaytimeMillis < elapsedMillis) {
            totalPlaytimeMillis = Long.MAX_VALUE;
        } else {
            totalPlaytimeMillis += elapsedMillis;
        }
        lastSaveSuccessful = saveProgress();
        if (!lastSaveSuccessful) return false;
        processDueRewards();
        return lastSaveSuccessful;
    }

    public synchronized List<PlaytimeRewardStatus> getRewardStatuses() {
        if (!loaded) return Collections.emptyList();
        List<PlaytimeRewardStatus> statuses = new ArrayList<PlaytimeRewardStatus>();
        for (PlaytimeRewardDefinition definition : PlaytimeRewardDefinition.getAll()) {
            long threshold = definition.getThresholdMillis();
            if (definition.isRecurring()) {
                long intervals = totalPlaytimeMillis / threshold;
                long progress = totalPlaytimeMillis % threshold;
                statuses.add(new PlaytimeRewardStatus(definition, progress,
                        threshold - progress, intervals, intervals > 0L
                        && (definition == PlaytimeRewardDefinition.DAILY
                        ? dailyIntervalsAwarded >= intervals
                        : tenDayIntervalsAwarded >= intervals)));
            } else {
                boolean earned = claimedMilestones.contains(definition.getId());
                long progress = Math.min(totalPlaytimeMillis, threshold);
                statuses.add(new PlaytimeRewardStatus(definition, progress,
                        Math.max(0L, threshold - totalPlaytimeMillis), 0L, earned));
            }
        }
        return Collections.unmodifiableList(statuses);
    }

    public synchronized long getTotalPlaytimeMillis() { return totalPlaytimeMillis; }
    public synchronized String getActiveProfileId() { return activeProfileId; }
    public synchronized boolean isLoaded() { return loaded; }
    public synchronized boolean wasLastSaveSuccessful() { return lastSaveSuccessful; }

    public synchronized void save() {
        if (!loaded || profileFile == null) return;
        lastSaveSuccessful = saveProgress();
        coinsCore.save();
    }

    private void processDueRewards() {
        long dailyDue = totalPlaytimeMillis / DAY_MILLIS;
        while (dailyIntervalsAwarded < dailyDue) {
            if (!award(PlaytimeRewardDefinition.DAILY)) return;
            dailyIntervalsAwarded++;
            if (!saveProgress()) return;
        }

        long tenDayMillis = PlaytimeRewardDefinition.TEN_DAYS.getThresholdMillis();
        long tenDayDue = totalPlaytimeMillis / tenDayMillis;
        while (tenDayIntervalsAwarded < tenDayDue) {
            if (!award(PlaytimeRewardDefinition.TEN_DAYS)) return;
            tenDayIntervalsAwarded++;
            if (!saveProgress()) return;
        }

        for (PlaytimeRewardDefinition definition : PlaytimeRewardDefinition.getAll()) {
            if (definition.isRecurring() || claimedMilestones.contains(definition.getId())
                    || totalPlaytimeMillis < definition.getThresholdMillis()) continue;
            if (!award(definition)) return;
            claimedMilestones.add(definition.getId());
            if (!saveProgress()) return;
        }
    }

    private boolean award(PlaytimeRewardDefinition definition) {
        return coinsCore.getService().addCoins(definition.getRewardCoins(),
                "Playtime reward: " + definition.getTitle());
    }

    private boolean saveProgress() {
        return storage.save(profileFile, totalPlaytimeMillis, dailyIntervalsAwarded,
                tenDayIntervalsAwarded, claimedMilestones);
    }

    private static String safeFileName(String value) {
        return value.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
