package com.alpharip.staffmod.modules;

import com.alpharip.staffmod.permissions.PermissionService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registry for AlphaClient modules. Only features marked implemented can
 * be toggled; planned features are displayed as Coming Soon rather than
 * pretending to work.
 */
public final class AlphaModuleRegistry {
    private static final List<AlphaModule> MODULES = new ArrayList<AlphaModule>();

    static {
        AlphaModule alphaHud = new AlphaModule(
                "alpha_hud", "Alpha HUD",
                "Displays the AlphaClient brand and session status.",
                AlphaModule.Category.PUBLIC, true, true);
        alphaHud.addFeature(new AlphaModuleFeature(
                "basic_hud", "Basic HUD", "AlphaClient branding and rank status.",
                PermissionService.Rank.PUBLIC, true));
        alphaHud.addFeature(new AlphaModuleFeature(
                "beta_hud_widgets", "Advanced HUD Widgets", "Additional HUD widgets for Beta testers.",
                PermissionService.Rank.BETA, false));
        alphaHud.addFeature(new AlphaModuleFeature(
                "donator_hud_themes", "Custom HUD Themes", "Extra client themes for Donators.",
                PermissionService.Rank.DONATOR, false));
        MODULES.add(alphaHud);
        MODULES.add(new AlphaModule(
                "fps_counter", "FPS Counter",
                "Displays the current Minecraft frame rate.",
                AlphaModule.Category.PUBLIC, true, true));
        MODULES.add(new AlphaModule(
                "keystrokes", "Keystrokes",
                "Configurable movement-key overlay.",
                AlphaModule.Category.PUBLIC, true, false));
        MODULES.add(new AlphaModule(
                "motion_blur", "Motion Blur",
                "Optional motion-blur visual effect.",
                AlphaModule.Category.BETA, false, false));
        MODULES.add(new AlphaModule(
                "cosmetics", "Alpha Cosmetics",
                "Community cosmetics and client-side appearance options.",
                AlphaModule.Category.DONATOR, false, false));
        AlphaModule staffmods = new AlphaModule(
                "staffmods", "Staffmods",
                "Staff-only module collection. BHOP is currently the retained feature.",
                AlphaModule.Category.STAFF, true, false);
        staffmods.addFeature(new AlphaModuleFeature(
                "bhop", "BHOP", "Retained staff movement module.",
                PermissionService.Rank.STAFF, true));
        MODULES.add(staffmods);
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