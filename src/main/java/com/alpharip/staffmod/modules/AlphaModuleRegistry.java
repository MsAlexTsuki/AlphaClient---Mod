package com.alpharip.staffmod.modules;

import com.alpharip.staffmod.permissions.PermissionService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Central registry for modules shown in the AlphaClient module menu.
 * Categories are declared by AlphaModule.Category; individual modules are
 * registered here and are only shown inside their assigned category.
 */
public final class AlphaModuleRegistry {
    private static final List<AlphaModule> MODULES = new ArrayList<AlphaModule>();

    static {
        AlphaModule bhop = new AlphaModule(
                "staffmods",
                "BHOP",
                "Retained AlphaClient movement module.",
                AlphaModule.Category.MOD,
                true,
                false
        );
        bhop.addFeature(new AlphaModuleFeature(
                "bhop",
                "BHOP",
                "Retained movement module.",
                PermissionService.Rank.MOD,
                true
        ));
        MODULES.add(bhop);
    }

    private AlphaModuleRegistry() {
    }

    public static List<AlphaModule> getAll() {
        return Collections.unmodifiableList(MODULES);
    }

    public static List<AlphaModule> getForCategory(AlphaModule.Category category) {
        List<AlphaModule> result = new ArrayList<AlphaModule>();
        for (AlphaModule module : MODULES) {
            if (module.getCategory() == category) {
                result.add(module);
            }
        }
        return result;
    }

    public static AlphaModule find(String id) {
        for (AlphaModule module : MODULES) {
            if (module.getId().equals(id)) {
                return module;
            }
        }
        return null;
    }

    public static boolean hasAccess(PermissionService.Rank rank) {
        return PermissionService.hasAccess(rank);
    }
}
