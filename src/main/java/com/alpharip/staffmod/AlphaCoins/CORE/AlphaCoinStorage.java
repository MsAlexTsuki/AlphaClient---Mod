package com.alpharip.staffmod.AlphaCoins.CORE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Local versioned persistence for AlphaCoins, including migration from the first Daily Challenges wallet format. */
public final class AlphaCoinStorage {
    private static final int VERSION = 1;
    private static final int MAX_HISTORY = 100;

    public Snapshot load(File file, File legacyDailyChallengeFile) {
        if (file != null && file.isFile()) {
            Snapshot current = read(file);
            if (current != null) return current;
        }
        Snapshot legacy = legacyDailyChallengeFile == null ? null : read(legacyDailyChallengeFile);
        return legacy == null ? new Snapshot() : legacy;
    }

    public boolean save(File file, AlphaCoinWallet wallet) {
        if (file == null || wallet == null) return false;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.isDirectory()) return false;

        Properties properties = new Properties();
        properties.setProperty("version", String.valueOf(VERSION));
        properties.setProperty("wallet.balance", String.valueOf(wallet.getBalance()));
        List<AlphaCoinTransaction> history = wallet.getHistory();
        int start = Math.max(0, history.size() - MAX_HISTORY);
        int count = history.size() - start;
        properties.setProperty("wallet.transaction.count", String.valueOf(count));
        for (int i = 0; i < count; i++) {
            AlphaCoinTransaction transaction = history.get(start + i);
            String prefix = "wallet.transaction." + i + ".";
            properties.setProperty(prefix + "amount", String.valueOf(transaction.getAmount()));
            properties.setProperty(prefix + "reason", transaction.getReason());
            properties.setProperty(prefix + "timestamp", String.valueOf(transaction.getTimestamp()));
        }

        File temporary = new File(parent, file.getName() + ".tmp");
        try (FileOutputStream output = new FileOutputStream(temporary)) {
            properties.store(output, "AlphaCoins local wallet data");
        } catch (IOException error) {
            temporary.delete();
            return false;
        }

        try {
            Files.move(temporary.toPath(), file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            return true;
        } catch (AtomicMoveNotSupportedException ignored) {
            try {
                Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return true;
            } catch (IOException error) {
                temporary.delete();
                return false;
            }
        } catch (IOException error) {
            temporary.delete();
            return false;
        }
    }

    private Snapshot read(File file) {
        if (file == null || !file.isFile()) return null;
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(file)) {
            properties.load(input);
            int version = parseInt(properties.getProperty("version"), 0);
            if (version != VERSION) return null;
            Snapshot snapshot = new Snapshot();
            snapshot.balance = Math.max(0L, parseLong(properties.getProperty("wallet.balance"), 0L));
            int count = clamp(parseInt(properties.getProperty("wallet.transaction.count"), 0), 0, MAX_HISTORY);
            for (int i = 0; i < count; i++) {
                String prefix = "wallet.transaction." + i + ".";
                long amount = parseLong(properties.getProperty(prefix + "amount"), 0L);
                if (amount != 0L) {
                    snapshot.history.add(new AlphaCoinTransaction(amount,
                            properties.getProperty(prefix + "reason", ""),
                            parseLong(properties.getProperty(prefix + "timestamp"), 0L)));
                }
            }
            return snapshot;
        } catch (IOException | RuntimeException ignored) {
            return null;
        }
    }

    private static int parseInt(String value, int fallback) {
        try { return value == null ? fallback : Integer.parseInt(value); }
        catch (RuntimeException ignored) { return fallback; }
    }

    private static long parseLong(String value, long fallback) {
        try { return value == null ? fallback : Long.parseLong(value); }
        catch (RuntimeException ignored) { return fallback; }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static final class Snapshot {
        private long balance;
        private final List<AlphaCoinTransaction> history = new ArrayList<AlphaCoinTransaction>();
        public long getBalance() { return balance; }
        public List<AlphaCoinTransaction> getHistory() { return new ArrayList<AlphaCoinTransaction>(history); }
    }
}
