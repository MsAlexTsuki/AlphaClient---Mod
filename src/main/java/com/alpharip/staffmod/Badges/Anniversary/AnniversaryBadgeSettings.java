package com.alpharip.staffmod.badges.anniversary;

import java.util.Date;

/**
 * Local prototype settings. The account creation date is entered manually until
 * an approved account-data integration exists. No Supabase or network calls.
 */
public final class AnniversaryBadgeSettings {
    private boolean enabled = true;
    private Long accountCreatedAtMillis;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public void setAccountCreatedAt(Date date) {
        accountCreatedAtMillis = date == null ? null : date.getTime();
    }

    public Date getAccountCreatedAt() {
        return accountCreatedAtMillis == null ? null : new Date(accountCreatedAtMillis);
    }

    public boolean hasAccountCreatedAt() { return accountCreatedAtMillis != null; }
    public void clearAccountCreatedAt() { accountCreatedAtMillis = null; }
}
