package com.alpharip.staffmod.AlphaDailyChallenges;

/** Immutable local wallet ledger entry. Positive amounts are credits; negative amounts are debits. */
public final class AlphaCoinTransaction {
    private final long amount;
    private final String reason;
    private final long timestamp;

    public AlphaCoinTransaction(long amount, String reason, long timestamp) {
        this.amount = amount;
        this.reason = reason == null ? "" : reason;
        this.timestamp = timestamp;
    }

    public long getAmount() { return amount; }
    public String getReason() { return reason; }
    public long getTimestamp() { return timestamp; }
}
