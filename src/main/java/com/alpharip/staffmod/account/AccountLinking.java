package com.alpharip.staffmod.account;

/**
 * Main facade for the Account Linking prototype.
 *
 * Entries are local references only. Real identity verification, authentication,
 * persistence, and account synchronization are intentionally outside this prototype.
 */
public final class AccountLinking {
    private final AccountLinkRegistry registry;
    private final AccountLinkingSettings settings;

    public AccountLinking() {
        this(new AccountLinkRegistry(), new AccountLinkingSettings());
    }

    public AccountLinking(AccountLinkRegistry registry, AccountLinkingSettings settings) {
        if (registry == null || settings == null) throw new IllegalArgumentException("Dependencies are required.");
        this.registry = registry;
        this.settings = settings;
    }

    public AccountLinkRegistry getRegistry() { return registry; }
    public AccountLinkingSettings getSettings() { return settings; }

    public void clearLocalData() {
        registry.clear();
        settings.clearSelection();
    }
}
