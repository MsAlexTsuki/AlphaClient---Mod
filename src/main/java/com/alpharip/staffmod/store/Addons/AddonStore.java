package com.alpharip.staffmod.store.addons;

import java.util.List;

/** Addons section facade for the central Store core. */
public final class AddonStore {
    private final AddonStoreSettings settings = new AddonStoreSettings();
    private final AddonRegistry registry = new AddonRegistry();
    private final AddonSearch search = new AddonSearch();
    private final AddonManager manager = new AddonManager(registry);

    public AddonStoreSettings getSettings() { return settings; }
    public AddonRegistry getRegistry() { return registry; }
    public AddonManager getManager() { return manager; }

    public List<AddonEntry> getVisibleAddons() {
        return search.search(registry.getAll(), settings.getSearchQuery(), settings.getSelectedCategory());
    }

    public boolean register(AddonEntry entry) { return registry.register(entry); }

    public void clear() {
        registry.clear();
        settings.setSearchQuery("");
        settings.setSelectedCategory(AddonCategory.ALL);
    }
}
