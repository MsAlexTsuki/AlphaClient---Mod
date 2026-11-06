package com.alpharip.staffmod.store.addons;

/**
 * Applies local state transitions to catalogue entries.
 * "Install" is a prototype flag only: it never downloads or executes add-on code.
 */
public final class AddonManager {
    private final AddonRegistry registry;

    public AddonManager(AddonRegistry registry) {
        if (registry == null) throw new IllegalArgumentException("Registry is required.");
        this.registry = registry;
    }

    public boolean markInstalled(String id) {
        AddonInstallState state = registry.getState(id);
        if (state != AddonInstallState.AVAILABLE && state != AddonInstallState.REMOVED) return false;
        return registry.setState(id, AddonInstallState.INSTALLED);
    }

    public boolean disable(String id) {
        if (registry.getState(id) != AddonInstallState.INSTALLED) return false;
        return registry.setState(id, AddonInstallState.DISABLED);
    }

    public boolean enable(String id) {
        if (registry.getState(id) != AddonInstallState.DISABLED) return false;
        return registry.setState(id, AddonInstallState.INSTALLED);
    }

    public boolean markRemoved(String id) {
        AddonInstallState state = registry.getState(id);
        if (state != AddonInstallState.INSTALLED && state != AddonInstallState.DISABLED) return false;
        return registry.setState(id, AddonInstallState.REMOVED);
    }
}
