package com.alpharip.staffmod.AlphaCoins.AlphaCoinsDailyChallenges;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Local prototype wallet. It is not an account entitlement or proof of a Store purchase. */
public final class AlphaCoinWallet {
    private static final int MAX_HISTORY = 100;
    private long balance;
    private final List<AlphaCoinTransaction> history = new ArrayList<AlphaCoinTransaction>();

    public synchronized long getBalance() { return balance; }

    /** Store-facing debit API. A failed debit leaves the balance unchanged. */
    public synchronized boolean spend(long amount, String reason) {
        if (amount <= 0L || balance < amount) return false;
        balance -= amount;
        addTransaction(new AlphaCoinTransaction(-amount, reason, System.currentTimeMillis()));
        return true;
    }

    synchronized void credit(long amount, String reason) {
        if (amount <= 0L) return;
        balance = balance > Long.MAX_VALUE - amount ? Long.MAX_VALUE : balance + amount;
        addTransaction(new AlphaCoinTransaction(amount, reason, System.currentTimeMillis()));
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
