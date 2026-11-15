package com.alpharip.staffmod.AlphaCoins.CORE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Local AlphaCoins balance and bounded transaction history. */
public final class AlphaCoinWallet {
    private static final int MAX_HISTORY = 100;
    private long balance;
    private final List<AlphaCoinTransaction> history = new ArrayList<AlphaCoinTransaction>();

    public synchronized long getBalance() { return balance; }

    synchronized boolean spend(long amount, String reason) {
        if (amount <= 0L || balance < amount) return false;
        balance -= amount;
        addTransaction(new AlphaCoinTransaction(-amount, reason, System.currentTimeMillis()));
        return true;
    }

    synchronized boolean credit(long amount, String reason) {
        if (amount <= 0L || amount > Long.MAX_VALUE - balance) return false;
        balance += amount;
        addTransaction(new AlphaCoinTransaction(amount, reason, System.currentTimeMillis()));
        return true;
    }

    public synchronized List<AlphaCoinTransaction> getHistory() {
        return Collections.unmodifiableList(new ArrayList<AlphaCoinTransaction>(history));
    }

    synchronized void restore(long savedBalance, List<AlphaCoinTransaction> savedHistory) {
        balance = Math.max(0L, savedBalance);
        history.clear();
        if (savedHistory != null) {
            int start = Math.max(0, savedHistory.size() - MAX_HISTORY);
            for (int i = start; i < savedHistory.size(); i++) {
                AlphaCoinTransaction transaction = savedHistory.get(i);
                if (transaction != null) history.add(transaction);
            }
        }
    }

    private void addTransaction(AlphaCoinTransaction transaction) {
        history.add(transaction);
        while (history.size() > MAX_HISTORY) history.remove(0);
    }
}
