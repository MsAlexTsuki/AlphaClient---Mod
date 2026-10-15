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
        AlphaModule quality = new AlphaModule(
                "quality",
                "Quality",
                "Quality of Life tools and planned features.",
                AlphaModule.Category.PUBLIC,
                true,
                true
        );
        quality.addFeature(new AlphaModuleFeature("alpha-module-manager", "Alpha Module Manager", "Central module management.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("playtime-tracker", "Playtime Tracker", "Track time spent playing.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("auto-gg", "Auto GG", "Automatically send GG after a game.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("keybind-profiles", "Keybind Profiles", "Save and switch keybind sets.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("auto-respawn", "Auto Respawn", "Automatically respawn after death.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("core-experience", "AlphaClient Core Experience", "Shared quality-of-life foundation.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("afk-detector", "AFK Detector", "Detect AFK state.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("mod-profiles", "Mod Profiles", "Save and switch module configurations.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("config-manager", "Config Manager", "Manage AlphaClient configuration files.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("auto-reconnect", "Auto Reconnect", "Reconnect after an unexpected disconnect.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("system-monitor", "Alpha System Monitor", "Show client and system diagnostics.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("crash-reporter", "Alpha Crash Reporter", "Collect crash diagnostics.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("update-checker", "Alpha Update Checker", "Check AlphaClient updates.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("control-center", "Alpha Control Center", "Central place for AlphaClient controls.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("keybind-manager", "Keybind Manager", "Configure AlphaClient keybinds.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("config-profiles", "Config Profiles", "Maintain named configuration profiles.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("session-timer", "Session Timer", "Track the current play session.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("feature-flags", "Alpha Feature Flags", "Control experimental feature availability.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("mod-toggle-menu", "Mod Toggle Menu", "Quickly enable or disable modules.", PermissionService.Rank.PUBLIC, false));
        MODULES.add(quality);

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
