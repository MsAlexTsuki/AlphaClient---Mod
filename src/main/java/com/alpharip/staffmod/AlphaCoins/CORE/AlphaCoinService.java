package com.alpharip.staffmod.AlphaCoins.CORE;

/** Public operation layer for awarding, spending, and querying AlphaCoins. */
public final class AlphaCoinService {
    private final AlphaCoinsCore core;

    AlphaCoinService(AlphaCoinsCore core) {
        if (core == null) throw new IllegalArgumentException("AlphaCoins core is required.");
        this.core = core;
    }

    public boolean addCoins(long amount, String reason) {
        return core.addCoins(amount, reason);
    }

    public boolean spendCoins(long amount, String reason) {
        return core.spendCoins(amount, reason);
    }

    public long getBalance() {
        return core.getWallet().getBalance();
    }

    public java.util.List<AlphaCoinTransaction> getTransactions() {
        return core.getWallet().getHistory();
    }
}
