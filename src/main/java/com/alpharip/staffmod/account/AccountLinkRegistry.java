package com.alpharip.staffmod.account;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** In-memory registry for local account references. */
public final class AccountLinkRegistry {
    private final Map<UUID, AccountLink> entries = new LinkedHashMap<UUID, AccountLink>();

    public synchronized boolean add(AccountLink entry) {
        if (entry == null || entries.containsKey(entry.getId()) || containsIdentity(entry.getProvider(), entry.getAccountIdentifier())) {
            return false;
        }
        entries.put(entry.getId(), entry);
        return true;
    }

    public synchronized AccountLink get(UUID id) {
        return id == null ? null : entries.get(id);
    }

    public synchronized boolean updateStatus(UUID id, AccountLinkStatus status) {
        AccountLink current = get(id);
        if (current == null || status == null) return false;
        entries.put(id, current.withStatus(status));
        return true;
    }

    public synchronized boolean remove(UUID id) {
        return id != null && entries.remove(id) != null;
    }

    public synchronized boolean containsIdentity(String provider, String accountIdentifier) {
        if (provider == null || accountIdentifier == null) return false;
        for (AccountLink entry : entries.values()) {
            if (entry.getProvider().equalsIgnoreCase(provider.trim())
                    && entry.getAccountIdentifier().equalsIgnoreCase(accountIdentifier.trim())) {
                return true;
            }
        }
        return false;
    }

    public synchronized List<AccountLink> getEntries() {
        return Collections.unmodifiableList(new ArrayList<AccountLink>(entries.values()));
    }

    public synchronized int size() { return entries.size(); }
    public synchronized void clear() { entries.clear(); }
}
