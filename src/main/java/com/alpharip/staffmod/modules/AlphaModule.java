package com.alpharip.staffmod.modules;

import com.alpharip.staffmod.permissions.PermissionService;
import com.alpharip.staffmod.config.AlphaClientConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Small module descriptor used by the new AlphaClient module menu.
 * Module implementations can extend this class as they are added.
 */
public final class AlphaModule {
    public enum Category {
        PUBLIC("Public Mods", PermissionService.Rank.PUBLIC),
        BETA("Beta Mods", PermissionService.Rank.BETA),
        DONATOR("Donator", PermissionService.Rank.DONATOR),
        STAFF("Staff Mods", PermissionService.Rank.STAFF);

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
        if (implemented && PermissionService.hasAccess(category.getRequiredRank())) {
            if (this.enabled != enabled) {
                this.enabled = enabled;
                AlphaClientConfig.saveModuleState(id, enabled);
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