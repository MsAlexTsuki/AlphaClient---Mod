package com.alpharip.staffmod.account;

import java.util.UUID;

/** Coordinates local account-reference operations without authenticating accounts. */
public final class AccountLinkingService {
    private final AccountLinking accountLinking;

    public AccountLinkingService(AccountLinking accountLinking) {
        if (accountLinking == null) throw new IllegalArgumentException("accountLinking is required");
        this.accountLinking = accountLinking;
    }

    public AccountLink createLocalEntry(String provider, String accountIdentifier, String displayName) {
        String error = AccountLinkValidator.validate(provider, accountIdentifier, displayName);
        if (error != null) throw new IllegalArgumentException(error);
        AccountLink entry = new AccountLink(UUID.randomUUID(), provider.trim(),
                accountIdentifier.trim(), displayName.trim(), System.currentTimeMillis(),
                AccountLinkStatus.LOCAL_ENTRY);
        if (!accountLinking.getRegistry().add(entry)) {
            throw new IllegalArgumentException("This provider and account identifier already exist.");
        }
        accountLinking.getSettings().setSelectedEntryId(entry.getId());
        return entry;
    }

    public boolean disconnectLocalEntry(UUID id) {
        boolean changed = accountLinking.getRegistry().updateStatus(id, AccountLinkStatus.DISCONNECTED);
        if (changed && id.equals(accountLinking.getSettings().getSelectedEntryId())) {
            accountLinking.getSettings().clearSelection();
        }
        return changed;
    }

    public boolean removeLocalEntry(UUID id) {
        boolean removed = accountLinking.getRegistry().remove(id);
        if (removed && id.equals(accountLinking.getSettings().getSelectedEntryId())) {
            accountLinking.getSettings().clearSelection();
        }
        return removed;
    }

    public boolean select(UUID id) {
        AccountLink entry = accountLinking.getRegistry().get(id);
        if (entry == null || !entry.isLocallyConnected()) return false;
        accountLinking.getSettings().setSelectedEntryId(id);
        return true;
    }

    public AccountLinking getAccountLinking() { return accountLinking; }
}
