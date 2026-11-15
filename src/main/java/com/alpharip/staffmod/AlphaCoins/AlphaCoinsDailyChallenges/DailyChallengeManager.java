package com.alpharip.staffmod.AlphaCoins.AlphaCoinsDailyChallenges;

import com.alpharip.staffmod.AlphaCoins.CORE.AlphaCoinsCore;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Coordinates daily rotation and progress; all currency operations are delegated to AlphaCoins CORE. */
public final class DailyChallengeManager {
    private final File configDirectory;
    private final File dataDirectory;
    private final DailyChallengeStorage storage = new DailyChallengeStorage();
    private final AlphaCoinsCore coinsCore = AlphaCoinsCore.getInstance();
    private final Map<String, DailyChallengeProgress> challenges =
            new LinkedHashMap<String, DailyChallengeProgress>();

    private File profileFile;
    private String activeProfileId;
    private LocalDate activeDate;
    private boolean loaded;
    private boolean lastSaveSuccessful = true;

    public DailyChallengeManager(File configDirectory) {
        if (configDirectory == null) throw new IllegalArgumentException("Config directory is required.");
        this.configDirectory = configDirectory;
        this.dataDirectory = new File(configDirectory, "daily-challenges");
    }

    public synchronized void activateProfile(String profileId) {
        if (profileId == null || profileId.trim().isEmpty()) return;
        String normalized = profileId.trim();
        if (loaded && normalized.equals(activeProfileId)) {
            ensureCurrentDay();
            return;
        }
        save();
        activeProfileId = normalized;
        profileFile = new File(dataDirectory, safeFileName(normalized) + ".properties");

        // CORE imports the wallet from the legacy challenge file once, then owns its own wallet file.
        coinsCore.activateProfile(normalized, configDirectory, profileFile);

        DailyChallengeStorage.Snapshot snapshot = storage.load(profileFile);
        activeDate = LocalDate.now();
        createDailyChallenges(activeDate);

        if (activeDate.equals(snapshot.date)) {
            for (Map.Entry<String, DailyChallengeStorage.SavedProgress> entry : snapshot.progress.entrySet()) {
                DailyChallengeProgress progress = challenges.get(entry.getKey());
                if (progress != null) {
                    DailyChallengeStorage.SavedProgress saved = entry.getValue();
                    progress.restore(saved.progress, saved.claimed);
                }
            }
        }
        loaded = true;
        save();
    }

    public synchronized List<DailyChallengeProgress> getChallenges() {
        ensureCurrentDay();
        return Collections.unmodifiableList(new ArrayList<DailyChallengeProgress>(challenges.values()));
    }

    public long getCoinBalance() {
        return coinsCore.getService().getBalance();
    }

    public synchronized boolean record(ChallengeMetric metric, int amount) {
        ensureCurrentDay();
        if (!loaded || metric == null || amount <= 0) return false;
        boolean changed = false;
        for (DailyChallengeProgress progress : challenges.values()) {
            if (progress.getDefinition().getMetric() == metric) {
                changed |= progress.addProgress(amount);
            }
        }
        if (changed) save();
        return changed;
    }

    public synchronized boolean claim(String challengeId) {
        ensureCurrentDay();
        if (!loaded || challengeId == null) return false;
        DailyChallengeProgress progress = challenges.get(challengeId);
        if (progress == null || !progress.isComplete() || progress.isClaimed()) return false;
        if (!coinsCore.getService().addCoins(progress.getDefinition().getReward(),
                "Daily challenge: " + progress.getDefinition().getTitle())) {
            return false;
        }
        progress.markClaimed();
        save();
        return true;
    }

    /** Local debit API for future integrations; this method does not perform a Store purchase. */
    public synchronized boolean spendCoins(long amount, String reason) {
        ensureCurrentDay();
        return loaded && coinsCore.getService().spendCoins(amount, reason);
    }

    public synchronized String getActiveProfileId() { return activeProfileId; }
    public synchronized boolean isLoaded() { return loaded; }
    public synchronized boolean wasLastSaveSuccessful() {
        return lastSaveSuccessful && coinsCore.wasLastSaveSuccessful();
    }

    public synchronized void save() {
        if (!loaded || profileFile == null || activeDate == null) return;
        lastSaveSuccessful = storage.save(profileFile, activeDate,
                new ArrayList<DailyChallengeProgress>(challenges.values()));
        coinsCore.save();
    }

    private void ensureCurrentDay() {
        if (!loaded) return;
        LocalDate today = LocalDate.now();
        if (!today.equals(activeDate)) {
            activeDate = today;
            createDailyChallenges(today);
            save();
        }
    }

    private void createDailyChallenges(LocalDate date) {
        challenges.clear();
        for (DailyChallengeDefinition definition : DailyChallengeCatalog.forDate(date)) {
            challenges.put(definition.getId(), new DailyChallengeProgress(definition));
        }
    }

    private static String safeFileName(String value) {
        return value.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
