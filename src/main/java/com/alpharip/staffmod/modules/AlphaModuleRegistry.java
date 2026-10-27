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
    /** All modules are disabled by default; profiles can opt in explicitly. */
    public static final boolean DEFAULT_MODULE_ENABLED = false;

    private static final List<AlphaModule> MODULES = new ArrayList<AlphaModule>();

    static {
        AlphaModule quality = new AlphaModule(
                "quality",
                "Quality",
                "Quality of Life tools and planned features.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        );
        quality.addFeature(new AlphaModuleFeature("alpha-module-manager", "Alpha Module Manager", "Central module management.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("playtime-tracker", "Playtime Tracker", "Track time spent playing.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("auto-gg", "Auto GG", "Automatically send GG after a detected kill or match end.", PermissionService.Rank.PUBLIC, true));
        quality.addFeature(new AlphaModuleFeature("keybind-profiles", "Keybind Profiles", "Save and switch keybind sets.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("auto-respawn", "Auto Respawn", "Automatically respawn after death.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("core-experience", "AlphaClient Core Experience", "Shared quality-of-life foundation.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("afk-detector", "AFK Detector", "Detect AFK state.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("mod-profiles", "Mod Profiles", "Save and switch module configurations.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("config-manager", "Config Manager", "Manage AlphaClient configuration files.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("auto-reconnect", "Auto Reconnect", "Reconnect after an unexpected disconnect.", PermissionService.Rank.PUBLIC, true));
        quality.addFeature(new AlphaModuleFeature("system-monitor", "Alpha System Monitor", "Show client and system diagnostics.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("crash-reporter", "Alpha Crash Reporter", "Collect crash diagnostics.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("update-checker", "Alpha Update Checker", "Check AlphaClient updates.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("control-center", "Alpha Control Center", "Central place for AlphaClient controls.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("keybind-manager", "Keybind Manager", "Configure keybinds.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("config-profiles", "Config Profiles", "Maintain named configuration profiles.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("session-timer", "Session Timer", "Track elapsed time while the Minecraft client is running.", PermissionService.Rank.PUBLIC, true));
        quality.addFeature(new AlphaModuleFeature("feature-flags", "Alpha Feature Flags", "Control experimental feature availability.", PermissionService.Rank.PUBLIC, false));
        quality.addFeature(new AlphaModuleFeature("mod-toggle-menu", "Mod Toggle Menu", "Quickly enable or disable modules.", PermissionService.Rank.PUBLIC, false));
        MODULES.add(quality);

        AlphaModule nameViewer = new AlphaModule(
                "name-viewer",
                "Name Viewer",
                "Show your own Minecraft name above your character; other players' rank badges remain automatic.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        );
        MODULES.add(nameViewer);

        AlphaModule fps = new AlphaModule(
                "fps",
                "FPS",
                "Compact FPS counter; client-wide performance optimization runs automatically.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        );
        MODULES.add(fps);

        AlphaModule ping = new AlphaModule(
                "ping",
                "Ping",
                "Show your current server latency in milliseconds with configurable HUD appearance.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        );
        MODULES.add(ping);

        MODULES.add(new AlphaModule(
                "armor-hud-info",
                "Armor HUD",
                "Show held-item and equipped-armor durability in a movable horizontal or vertical HUD.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "potion-hud-info",
                "Potion HUD",
                "Display active potion effects with four selectable display modes.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        AlphaModule legacyPvp = new AlphaModule(
                "legacy-pvp",
                "Legacy PvP",
                "Legacy PvP presentation settings; each feature reports whether it is available, needs a client hook, or requires server support.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        );
        MODULES.add(legacyPvp);

        AlphaModule cps = new AlphaModule(
                "cps",
                "CPS",
                "Clicks per second HUD with selectable mouse buttons and appearance settings.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        );
        MODULES.add(cps);

        // Bhop is registered for access checks/settings only. The StaffMods menu
        // explicitly exposes it in General and filters it from every rank folder.
        MODULES.add(new AlphaModule(
                "staffmods",
                "Bhop",
                "Configure the existing AlphaClient Bhop module.",
                AlphaModule.Category.T_MOD,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        AlphaModule wTap = new AlphaModule(
                "w-tap-assistance",
                "W-Tap Assistance",
                "Configure W-Tap Assistance options.",
                AlphaModule.Category.FOUNDER,
                true,
                DEFAULT_MODULE_ENABLED
        );
        MODULES.add(wTap);

        AlphaModule sTap = new AlphaModule(
                "s-tap-assistance",
                "S-Tap Assistance",
                "Randomizable W-key tap style; does not require the S key.",
                AlphaModule.Category.FOUNDER,
                true,
                DEFAULT_MODULE_ENABLED
        );
        MODULES.add(sTap);

        // AimAssist is visible and usable only for the authenticated Founder account.
        MODULES.add(new AlphaModule(
                "aim-assist",
                "AimAssist",
                "Smoothly correct aim only when a visible player is already close to the crosshair.",
                AlphaModule.Category.FOUNDER,
                true,
                DEFAULT_MODULE_ENABLED
        ));
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
