package com.alpharip.staffmod.modules;

import com.alpharip.staffmod.permissions.PermissionService;
import com.alpharip.staffmod.config.AlphaClientConfig;
import com.alpharip.staffmod.profile.ModuleProfileManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Small module descriptor used by the new AlphaClient module menu.
 * Module implementations can extend this class as they are added.
 */
public final class AlphaModule {
    public enum Category {
        PUBLIC("General", PermissionService.Rank.PUBLIC),
        BETA("Beta", PermissionService.Rank.BETA),
        DONATOR("Donator", PermissionService.Rank.DONATOR),
        T_MOD("T-Mod", PermissionService.Rank.T_MOD),
        MOD("Mod", PermissionService.Rank.MOD),
        SR_MOD("Sr-Mod", PermissionService.Rank.SR_MOD),
        ADMIN("Admin", PermissionService.Rank.ADMIN),
        SR_ADMIN("Sr-Admin", PermissionService.Rank.SR_ADMIN),
        MANAGEMENT("Management", PermissionService.Rank.PLAT_ADMIN),
        PLAT_ADMIN("Platform Admin", PermissionService.Rank.PLAT_ADMIN),
        MANAGER("Manager", PermissionService.Rank.MANAGER),
        OWNER("Owner", PermissionService.Rank.OWNER),
        FOUNDER("Founder", PermissionService.Rank.FOUNDER);

        private final String title;
        private final PermissionService.Rank requiredRank;

        Category(String title, PermissionService.Rank requiredRank) {
            this.title = title;
            this.requiredRank = requiredRank;
        }

        public String getTitle() { return title; }
        public PermissionService.Rank getRequiredRank() { return requiredRank; }
    }

    private final String id;
    private final String name;
    private final String description;
    private final Category category;
    private final boolean implemented;
    private boolean enabled;
    private final List<AlphaModuleFeature> features = new ArrayList<AlphaModuleFeature>();

    public AlphaModule(String id, String name, String description,
                       Category category, boolean implemented, boolean defaultEnabled) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.implemented = implemented;
        this.enabled = defaultEnabled;
    }

    public void addFeature(AlphaModuleFeature feature) {
        if (feature != null) features.add(feature);
    }

    public List<AlphaModuleFeature> getFeatures() {
        return Collections.unmodifiableList(features);
    }

    public AlphaModuleFeature getFeature(String featureId) {
        for (AlphaModuleFeature feature : features) {
            if (feature.getId().equals(featureId)) return feature;
        }
        return null;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public boolean isImplemented() { return implemented; }

    /** Effective state: a saved ON choice cannot activate while access is locked. */
    public boolean isEnabled() { return enabled && isUnlocked(); }

    /** Raw saved preference, used only when serializing/restoring configuration. */
    public boolean isConfiguredEnabled() { return enabled; }

    /** Restores a saved preference without treating startup as a user toggle. */
    public void restoreEnabledState(boolean enabled) {
        if (implemented) this.enabled = enabled;
    }

    public void setEnabled(boolean enabled) {
        if (implemented && (!enabled || isUnlocked())) {
            if (this.enabled != enabled) {
                this.enabled = enabled;
                AlphaClientConfig.saveModuleState(id, enabled);
                if (!ModuleProfileManager.isApplyingModuleStates()) {
                    ModuleProfileManager.setSelectedModuleEnabled(id, enabled);
                }
            }
        }
    }

    private String getPermissionId() {
        // These stable IDs match the current AlphaLauncher permission catalog.
        if ("fps".equals(id)) return "public.performance_monitor";
        if ("cps".equals(id)) return "public.custom_hud";
        if ("staffmods".equals(id)) return "staff.bhop";
        return id;
    }

    /** True only when this exact module has an explicit authenticated allow grant. */
    public boolean hasExplicitModAccess() {
        String permissionId = getPermissionId();
        return PermissionService.hasExplicitModPermission(permissionId)
                && PermissionService.hasModAccess(permissionId);
    }

    public boolean isUnlocked() {
        // Founder-only modules remain exclusive and cannot be granted to other ranks.
        if (category == Category.FOUNDER) {
            return PermissionService.hasAccess(PermissionService.Rank.FOUNDER);
        }

        // General modules remain public.
        if (category == Category.PUBLIC) {
            return true;
        }

        // Owner and Founder retain their normal access to non-Founder modules.
        if (PermissionService.hasAccess(PermissionService.Rank.FOUNDER)
                || PermissionService.hasAccess(PermissionService.Rank.OWNER)) {
            return true;
        }

        // A staff rank OR an explicit allow for this exact mod grants access.
        // Explicit per-mod grants are also used to reveal the module in its menu folder.
        return PermissionService.hasAccess(category.getRequiredRank()) || hasExplicitModAccess();
    }

    public String getStatusLabel() {
        if (!isUnlocked()) return "LOCKED";
        if (!implemented) return "COMING SOON";
        return enabled ? "ON" : "OFF";
    }
}