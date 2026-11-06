package com.alpharip.staffmod.account;

import java.util.UUID;

/** Immutable local account reference. No credentials or authentication tokens are stored. */
public final class AccountLink {
    private final UUID id;
    private final String provider;
    private final String accountIdentifier;
    private final String displayName;
    private final long createdAt;
    private final AccountLinkStatus status;

    public AccountLink(UUID id, String provider, String accountIdentifier,
                       String displayName, long createdAt, AccountLinkStatus status) {
        if (id == null || status == null) {
            throw new IllegalArgumentException("id and status are required");
        }
        this.id = id;
        this.provider = requireText(provider, "provider");
        this.accountIdentifier = requireText(accountIdentifier, "accountIdentifier");
        this.displayName = requireText(displayName, "displayName");
        if (createdAt < 0L) {
            throw new IllegalArgumentException("createdAt cannot be negative");
        }
        this.createdAt = createdAt;
        this.status = status;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    public UUID getId() { return id; }
    public String getProvider() { return provider; }
    public String getAccountIdentifier() { return accountIdentifier; }
    public String getDisplayName() { return displayName; }
    public long getCreatedAt() { return createdAt; }
    public AccountLinkStatus getStatus() { return status; }
    public boolean isLocallyConnected() { return status == AccountLinkStatus.LOCAL_ENTRY; }

    public AccountLink withStatus(AccountLinkStatus nextStatus) {
        return new AccountLink(id, provider, accountIdentifier, displayName, createdAt, nextStatus);
    }
}
