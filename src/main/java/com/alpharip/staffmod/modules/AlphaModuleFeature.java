// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.modules;

import com.alpharip.staffmod.permissions.PermissionService;

/** Permission tier for a feature inside a module, independent of module visibility. */
public final class AlphaModuleFeature {
    private final String id;
    private final String name;
    private final String description;
    private final PermissionService.Rank requiredRank;
    private final boolean implemented;

    public AlphaModuleFeature(String id, String name, String description,
                              PermissionService.Rank requiredRank, boolean implemented) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.requiredRank = requiredRank;
        this.implemented = implemented;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public PermissionService.Rank getRequiredRank() { return requiredRank; }
    public boolean isImplemented() { return implemented; }
    public boolean isAvailable() {
        return implemented && PermissionService.hasAccess(requiredRank);
    }
    public String getStatusLabel() {
        if (!PermissionService.hasAccess(requiredRank)) return "LOCKED";
        return implemented ? "AVAILABLE" : "COMING SOON";
    }
}