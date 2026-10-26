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
                "coordinates",
                "Coordinates HUD",
                "Show current block X, Y and Z coordinates in the General HUD.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "compass",
                "Compass HUD",
                "Show the player's cardinal direction and yaw in the General HUD.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "keystrokes",
                "Keystrokes",
                "Display live WASD and jump-key states in the General HUD.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "item-hud",
                "Item HUD",
                "Show the held item's name and remaining durability in the General HUD.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "status-dashboard",
                "Status Dashboard",
                "Show a compact live FPS and current-server status line.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "combat-warnings",
                "Combat Warnings",
                "Warn when health is low or the held item is nearly broken.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

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

        MODULES.add(new AlphaModule(
                "auto-sprint",
                "Auto Sprint",
                "Automatically sprint while moving forward when sprinting is safe.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "auto-tool",
                "Auto Tool",
                "Select the strongest hotbar tool when starting to mine a block and restore the previous slot on release.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "auto-respawn",
                "Auto Respawn",
                "Automatically respawn after death while enabled.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "low-health-warning",
                "Low Health Warning",
                "Display a compact warning when health drops below the configured first-pass threshold.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "potion-expiration-warning",
                "Potion Expiration Warning",
                "Show active potion effects that are close to expiring.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "block-counter",
                "Block Counter",
                "Count blocks in the inventory and display the total on the HUD.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "death-waypoint",
                "Death Waypoint",
                "Save and announce the location where the player died.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "armor-durability-warning",
                "Armor Durability Warning",
                "Warn when equipped armor is close to breaking.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "target-distance-hud",
                "Target Distance HUD",
                "Show the distance to the entity currently targeted by the crosshair.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "nearest-enemy-hud",
                "Nearest Enemy HUD",
                "Show the nearest visible player within a short range.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "session-timer",
                "Session Timer",
                "Show the elapsed time since joining the current world or server.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "playtime-tracker",
                "Playtime Tracker",
                "Track elapsed play time for the current session.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "potion-hud",
                "Potion HUD",
                "Display active potion effects and their remaining duration.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "armor-hud",
                "Armor HUD",
                "Display equipped armor pieces and remaining durability.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "compass-hud",
                "Compass HUD",
                "Display the player's current horizontal facing direction.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "coordinates-hud",
                "Coordinates HUD",
                "Display the player's current world coordinates.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "target-health-hud",
                "Target Health HUD",
                "Show health for the entity under the crosshair.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "crosshair-customizer",
                "Crosshair Customizer",
                "Draw a simple configurable-style crosshair overlay.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "chat-timestamp",
                "Chat Timestamp",
                "Prefix incoming chat messages with the local time.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "kill-counter",
                "Kill Counter",
                "Count kills credited to the local player from client-visible death events.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "attack-indicator",
                "Attack Indicator",
                "Display the vanilla attack cooldown as a compact percentage.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "biome-hud",
                "Biome HUD",
                "Show the biome at the player's current position.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "light-level-hud",
                "Light Level HUD",
                "Show the block light level at the player's position.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "world-time-hud",
                "World Time HUD",
                "Show the current Minecraft world time.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "fall-distance-hud",
                "Fall Distance HUD",
                "Show the player's current fall distance.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule("velocity-hud", "Velocity HUD", "Display horizontal and vertical movement velocity.", AlphaModule.Category.PUBLIC, true, DEFAULT_MODULE_ENABLED));
        MODULES.add(new AlphaModule("durability-hud", "Held Item Durability", "Display remaining durability of the held item.", AlphaModule.Category.PUBLIC, true, DEFAULT_MODULE_ENABLED));
        MODULES.add(new AlphaModule("food-hud", "Food HUD", "Display current food level.", AlphaModule.Category.PUBLIC, true, DEFAULT_MODULE_ENABLED));
        MODULES.add(new AlphaModule("health-hud", "Health HUD", "Display current and maximum health.", AlphaModule.Category.PUBLIC, true, DEFAULT_MODULE_ENABLED));
        MODULES.add(new AlphaModule("experience-hud", "Experience HUD", "Display experience level and progress.", AlphaModule.Category.PUBLIC, true, DEFAULT_MODULE_ENABLED));
        MODULES.add(new AlphaModule("air-hud", "Air HUD", "Display remaining air while underwater.", AlphaModule.Category.PUBLIC, true, DEFAULT_MODULE_ENABLED));
        MODULES.add(new AlphaModule("speed-hud", "Speed HUD", "Display horizontal movement speed in blocks per second.", AlphaModule.Category.PUBLIC, true, DEFAULT_MODULE_ENABLED));
        MODULES.add(new AlphaModule("dimension-hud", "Dimension HUD", "Display the current dimension identifier.", AlphaModule.Category.PUBLIC, true, DEFAULT_MODULE_ENABLED));
        MODULES.add(new AlphaModule("day-counter", "Day Counter", "Display the current Minecraft world day.", AlphaModule.Category.PUBLIC, true, DEFAULT_MODULE_ENABLED));
        MODULES.add(new AlphaModule("entity-count-hud", "Entity Count HUD", "Display nearby living entity count.", AlphaModule.Category.PUBLIC, true, DEFAULT_MODULE_ENABLED));
        MODULES.add(new AlphaModule("durability-warning", "Durability Warning", "Warn when the held item is nearly broken.", AlphaModule.Category.PUBLIC, true, DEFAULT_MODULE_ENABLED));
        MODULES.add(new AlphaModule("hunger-warning", "Hunger Warning", "Warn when the food bar is low.", AlphaModule.Category.PUBLIC, true, DEFAULT_MODULE_ENABLED));
        AlphaModule legacyPvp = new AlphaModule(
                "legacy-pvp",
                "Legacy PvP",
                "Legacy PvP presentation settings; each feature reports whether it is available, needs a client hook, or requires server support.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        );
        MODULES.add(legacyPvp);

        MODULES.add(new AlphaModule(
                "distance-tracker",
                "Distance Tracker",
                "Show the distance when you hit another player and when another player hits you.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "healthbar",
                "Healthbar",
                "Briefly show a health bar above the player you are looking at.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        // Initial working foundations. These are deliberately simple first passes;
        // tuneable settings and in-game validation can be added incrementally.
        MODULES.add(new AlphaModule(
                "aim-assist",
                "Aim Assist",
                "Founder-only aim correction for controlled testing.",
                AlphaModule.Category.FOUNDER,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "targeting",
                "Targeting",
                "Selects the nearest eligible player to the crosshair and displays target name and distance.",
                AlphaModule.Category.FOUNDER,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "attack-timing",
                "Attack Timing",
                "Shows the local hand-swing state; Minecraft 1.8.9 has no later-version attack cooldown meter.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

        MODULES.add(new AlphaModule(
                "hitbox",
                "Hitbox",
                "Visualizes existing player bounding boxes without changing collision geometry.",
                AlphaModule.Category.PUBLIC,
                true,
                DEFAULT_MODULE_ENABLED
        ));

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

        // FOUNDER ONLY: S-Tap mirrors W-Tap Assistance, but uses the S/back key.
        AlphaModule sTap = new AlphaModule(
                "s-tap-assistance",
                "S-Tap",
                "Founder-only S-Tap assistance; briefly releases the S key after an attack.",
                AlphaModule.Category.FOUNDER,
                true,
                DEFAULT_MODULE_ENABLED
        );
        MODULES.add(sTap);
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
