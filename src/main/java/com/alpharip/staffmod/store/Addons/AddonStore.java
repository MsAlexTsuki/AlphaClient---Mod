package com.alpharip.staffmod.store.addons;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Addons catalogue, filtering, state transitions, and optional local state persistence. */
public final class AddonStore {
    private final AddonStoreSettings settings = new AddonStoreSettings();
    private final AddonRegistry registry = new AddonRegistry();
    private final AddonSearch search = new AddonSearch();
    private final AddonManager manager = new AddonManager(registry);
    private final AddonStoreStorage storage;
    private final Map<String, AddonInstallState> pendingStates = new LinkedHashMap<String, AddonInstallState>();
    private String storageWarning = "";

    public AddonStore() {
        storage = null;
    }

    /** Loads saved local states and applies them when matching trusted entries are registered. */
    public AddonStore(Path stateFile) {
        if (stateFile == null) throw new IllegalArgumentException("State file path is required.");
        storage = new AddonStoreStorage(stateFile);
        try {
            pendingStates.putAll(storage.loadStates());
        } catch (IOException exception) {
            storageWarning = "Could not load local Store state: " + exception.getMessage();
        }
    }

    public AddonStoreSettings getSettings() { return settings; }
    public AddonRegistry getRegistry() { return registry; }
    public AddonManager getManager() { return manager; }
    public String getStorageWarning() { return storageWarning; }

    public List<AddonEntry> getVisibleAddons() {
        List<AddonEntry> matches = search.search(registry.getAll(),
                settings.getSearchQuery(), settings.getSelectedCategory());
        if (!settings.isShowInstalledOnly()) return matches;
        List<AddonEntry> installed = new ArrayList<AddonEntry>();
        for (AddonEntry entry : matches) {
            AddonInstallState state = registry.getState(entry.getId());
            if (state == AddonInstallState.INSTALLED || state == AddonInstallState.DISABLED)
                installed.add(entry);
        }
        return installed;
    }

    public boolean register(AddonEntry entry) {
        boolean added = registry.register(entry);
        if (added && entry != null) {
            AddonInstallState saved = pendingStates.remove(entry.getId());
            if (saved != null) registry.restoreState(entry.getId(), saved);
        }
        return added;
    }

    public boolean markInstalled(String id) { return changeState(manager.markInstalled(id)); }
    public boolean enable(String id) { return changeState(manager.enable(id)); }
    public boolean disable(String id) { return changeState(manager.disable(id)); }
    public boolean markRemoved(String id) { return changeState(manager.markRemoved(id)); }

    private boolean changeState(boolean changed) {
        if (changed) persistStates();
        return changed;
    }

    public boolean persistStates() {
        if (storage == null) return true;
        try {
            storage.save(registry, pendingStates);
            storageWarning = "";
            return true;
        } catch (IOException exception) {
            storageWarning = "Could not save local Store state: " + exception.getMessage();
            return false;
        }
    }

    public void clearSessionFilters() {
        settings.setSearchQuery("");
        settings.setSelectedCategory(AddonCategory.ALL);
        settings.setShowInstalledOnly(false);
    }

    /** Clears the in-memory catalogue and its saved local state. */
    public void clear() {
        registry.clear();
        pendingStates.clear();
        clearSessionFilters();
        if (storage != null) {
            try {
                storage.delete();
                storageWarning = "";
            } catch (IOException exception) {
                storageWarning = "Could not clear local Store state: " + exception.getMessage();
            }
        }
    }
}
