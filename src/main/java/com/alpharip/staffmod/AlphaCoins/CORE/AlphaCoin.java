package com.alpharip.staffmod.AlphaCoins.CORE;

/** AlphaCoins currency metadata and validated coin amounts. */
public final class AlphaCoin {
    public static final String CURRENCY_ID = "ALPHACOIN";
    public static final String DISPLAY_NAME = "AlphaCoin";

    private final long amount;

    public AlphaCoin(long amount) {
        if (amount <= 0L) throw new IllegalArgumentException("Coin amount must be positive.");
        this.amount = amount;
    }

    public long getAmount() { return amount; }
    public String getCurrencyId() { return CURRENCY_ID; }
    public String getDisplayName() { return DISPLAY_NAME; }
}
