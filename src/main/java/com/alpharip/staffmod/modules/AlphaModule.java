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

        public String getTitle() {
            return title;
        }

        public PermissionService.Rank getRequiredRank() {
            return requiredRank;
        }
    }

    private final String id;
    private final String name;
    private final String description;
    private final Category category;
    private final boolean implemented;
    private boolean enabled;
    private final List<AlphaModuleFeature> features = new ArrayList<AlphaModuleFeature>();

    public AlphaModule(String id, String name, String description,
                       Category category, boolean implemented,
                       boolean defaultEnabled) {
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
    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) {
        if (implemented && (!enabled || PermissionService.hasAccess(category.getRequiredRank()))) {
            if (this.enabled != enabled) {
                this.enabled = enabled;
                AlphaClientConfig.saveModuleState(id, enabled);
                if (!ModuleProfileManager.isApplyingModuleStates()) {
                    ModuleProfileManager.setSelectedModuleEnabled(id, enabled);
                }
            }
        }
    }

    public boolean isUnlocked() {
        return PermissionService.hasAccess(category.getRequiredRank());
    }

    public String getStatusLabel() {
        if (!isUnlocked()) {
            return "LOCKED";
        }
        if (!implemented) {
            return "COMING SOON";
        }
        return enabled ? "ON" : "OFF";
    }
}