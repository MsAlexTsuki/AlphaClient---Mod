package com.alpharip.staffmod.AlphaDailyChallenges;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Coordinates the daily rotation, progress, reward claims, and local AlphaCoins wallet. */
public final class DailyChallengeManager {
    private final File dataDirectory;
    private final DailyChallengeStorage storage = new DailyChallengeStorage();
    private final AlphaCoinWallet wallet = new AlphaCoinWallet();
    private final Map<String, DailyChallengeProgress> challenges =
            new LinkedHashMap<String, DailyChallengeProgress>();

    private File profileFile;
    private String activeProfileId;
    private LocalDate activeDate;
    private boolean loaded;
    private boolean lastSaveSuccessful = true;

    public DailyChallengeManager(File configDirectory) {
        if (configDirectory == null) throw new IllegalArgumentException("Config directory is required.");
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
        DailyChallengeStorage.Snapshot snapshot = storage.load(profileFile);
        wallet.restore(snapshot.balance, snapshot.history);
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

    public AlphaCoinWallet getWallet() {
        return wallet;
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
        if (progress == null || !progress.markClaimed()) return false;
        wallet.credit(progress.getDefinition().getReward(),
                "Daily challenge: " + progress.getDefinition().getTitle());
        save();
        return true;
    }

    /** Used by the future Store purchase flow; does not perform a purchase itself. */
    public synchronized boolean spendCoins(int amount, String reason) {
        ensureCurrentDay();
        if (!loaded || amount <= 0) return false;
        boolean spent = wallet.spend(amount, reason);
        if (spent) save();
        return spent;
    }

    public synchronized String getActiveProfileId() { return activeProfileId; }
    public synchronized boolean isLoaded() { return loaded; }
    public synchronized boolean wasLastSaveSuccessful() { return lastSaveSuccessful; }

    public synchronized void save() {
        if (!loaded || profileFile == null || activeDate == null) return;
        lastSaveSuccessful = storage.save(profileFile, activeDate, wallet,
                new ArrayList<DailyChallengeProgress>(challenges.values()));
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
