package com.alpharip.staffmod.AlphaCoins.CORE;

import java.io.File;

/**
 * Central AlphaCoins facade. Daily Challenges and future modules should use this API
 * instead of owning a separate wallet or transaction ledger.
 */
public final class AlphaCoinsCore {
    private static final AlphaCoinsCore INSTANCE = new AlphaCoinsCore();

    private final AlphaCoinWallet wallet = new AlphaCoinWallet();
    private final AlphaCoinStorage storage = new AlphaCoinStorage();
    private final AlphaCoinService service = new AlphaCoinService(this);
    private File walletFile;
    private String activeProfileId;
    private boolean loaded;
    private boolean lastSaveSuccessful = true;

    private AlphaCoinsCore() { }

    public static AlphaCoinsCore getInstance() { return INSTANCE; }

    public synchronized void activateProfile(String profileId, File configDirectory,
                                              File legacyDailyChallengeFile) {
        if (profileId == null || profileId.trim().isEmpty() || configDirectory == null) return;
        String normalized = profileId.trim();
        if (loaded && normalized.equals(activeProfileId)) return;
        save();
        File directory = new File(configDirectory, "alphacoins");
        walletFile = new File(directory, safeFileName(normalized) + ".properties");
        AlphaCoinStorage.Snapshot snapshot = storage.load(walletFile, legacyDailyChallengeFile);
        wallet.restore(snapshot.getBalance(), snapshot.getHistory());
        activeProfileId = normalized;
        loaded = true;
        lastSaveSuccessful = storage.save(walletFile, wallet);
    }

    public synchronized boolean addCoins(long amount, String reason) {
        if (!loaded || amount <= 0L) return false;
        boolean added = wallet.credit(amount, reason);
        if (added) save();
        return added;
    }

    public synchronized boolean spendCoins(long amount, String reason) {
        if (!loaded || amount <= 0L) return false;
        boolean spent = wallet.spend(amount, reason);
        if (spent) save();
        return spent;
    }

    synchronized long getBalance() { return wallet.getBalance(); }
    synchronized java.util.List<AlphaCoinTransaction> getTransactions() { return wallet.getHistory(); }
    public AlphaCoinService getService() { return service; }
    public synchronized String getActiveProfileId() { return activeProfileId; }
    public synchronized boolean isLoaded() { return loaded; }
    public synchronized boolean wasLastSaveSuccessful() { return lastSaveSuccessful; }

    public synchronized void save() {
        if (!loaded || walletFile == null) return;
        lastSaveSuccessful = storage.save(walletFile, wallet);
    }

    private static String safeFileName(String value) {
        return value.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
