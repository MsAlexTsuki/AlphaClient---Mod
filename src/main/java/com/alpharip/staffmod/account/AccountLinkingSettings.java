package com.alpharip.staffmod.account;

import java.util.UUID;

/** Session-local settings for the Account Linking prototype. */
public final class AccountLinkingSettings {
    private boolean enabled = true;
    private UUID selectedEntryId;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public UUID getSelectedEntryId() { return selectedEntryId; }
    public void setSelectedEntryId(UUID selectedEntryId) { this.selectedEntryId = selectedEntryId; }
    public void clearSelection() { selectedEntryId = null; }
}
