// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.permissions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Controls command discoverability only. It does not unregister commands or
 * prevent command execution; execution permissions remain in their handlers.
 */
public final class AlphaCommandVisibility {
    private static final Set<String> MEMBER = immutable(
            "fpshud", "alphaclient:afktimer", "alphaclient:modules", "alphaclient:fullbright",
            "fullbright", "playtime", "alphaclient:help", "alphahelp", "alphaclient:playtime",
            "hudeditor", "alphaclient:fps", "alphaclient:legacypvp", "afktimer", "profile",
            "profiles", "quality", "alphaclient:settings", "legacypvp", "alphaclient:cps",
            "general", "sessiontimer", "nameviewer", "autoreconnect", "cpshud", "autogg",
            "alphaclient:module", "pothud", "alphaclient", "alphaclient:sessiontimer");
    private static final Set<String> STAFF_COMMANDS = immutable(
            "staffgeneral", "bhop", "stafftmod", "staffmods", "staffmod", "staffsrmod",
            "staffadmin", "staffsradmin", "staffplatformadmin", "staffplatadmin",
            "staffmanagement", "staffmanager", "staffoperations", "alphanote", "staffowner",
            "stafffounder", "alphaperms", "wtap");
    private static final Set<String> DONATOR_MENU = immutable(
            "scout", "donator", "rogue", "elite", "alpha", "alphaplus", "beta");

    private AlphaCommandVisibility() { }

    private static Set<String> immutable(String... values) {
        return Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(values)));
    }

    public static boolean canShow(String rawCommand) {
        if (rawCommand == null) return false;
        String command = rawCommand.trim().toLowerCase(Locale.ROOT);
        while (command.startsWith("/")) command = command.substring(1);
        if (command.isEmpty()) return false;
        String[] parts = command.split("\\s+", 2);
        String root = parts[0];
        String args = parts.length > 1 ? parts[1] : "";

        PermissionService.Rank rank = PermissionService.getEffectiveRank();
        boolean founder = PermissionService.hasAccess(PermissionService.Rank.FOUNDER);
        boolean owner = PermissionService.hasAccess(PermissionService.Rank.OWNER);
        boolean developer = PermissionService.hasAccess(PermissionService.Rank.DEVELOPER);

        if (founder || developer) return true;
        if (owner) return !"wtap".equals(root) && !"stafffounder".equals(root)
                && !"alphaclient:wtap".equals(root);

        boolean member = MEMBER.contains(root);
        if (member) return true;

        if ("pixelgarden".equals(root) || "garden".equals(root)) {
            if (!PermissionService.hasAccess(PermissionService.Rank.SCOUT)) return false;
            String first = args.isEmpty() ? "" : args.split("\\s+", 2)[0];
            return !isFounderOnlyGardenRoute(first);
        }

        if (isStaff(rank)) {
            if (STAFF_COMMANDS.contains(root)) {
                if ("stafffounder".equals(root) || "wtap".equals(root)) return false;
                if ("staffgeneral".equals(root) || "bhop".equals(root)
                        || "stafftmod".equals(root) || "staffmods".equals(root)) {
                    return true;
                }
                if ("staffmod".equals(root)) return atLeast(rank, PermissionService.Rank.MOD);
                if ("staffsrmod".equals(root)) return atLeast(rank, PermissionService.Rank.SR_MOD);
                if ("staffadmin".equals(root)) return atLeast(rank, PermissionService.Rank.ADMIN);
                if ("staffsradmin".equals(root)) return atLeast(rank, PermissionService.Rank.SR_ADMIN);
                if ("staffplatformadmin".equals(root) || "staffplatadmin".equals(root)
                        || "staffmanagement".equals(root)) return atLeast(rank, PermissionService.Rank.PLAT_ADMIN);
                if ("staffmanager".equals(root) || "staffoperations".equals(root)
                        || "alphanote".equals(root)) return atLeast(rank, PermissionService.Rank.MANAGER);
                if ("alphaperms".equals(root)) return atLeast(rank, PermissionService.Rank.FOUNDER);
                return true;
            }
            if ("scout".equals(root) || "donator".equals(root)) return true;
            if ("rogue".equals(root)) return atLeast(rank, PermissionService.Rank.MOD);
            if ("elite".equals(root)) return atLeast(rank, PermissionService.Rank.SR_MOD);
            if ("alpha".equals(root)) return atLeast(rank, PermissionService.Rank.SR_MOD);
            if ("alphaplus".equals(root)) return atLeast(rank, PermissionService.Rank.ADMIN);
            // Beta is intentionally not assigned a visibility tier yet.
            return false;
        }
        if ("scout".equals(root) || "donator".equals(root)) {
            return PermissionService.hasAccess(PermissionService.Rank.SCOUT);
        }
        if ("rogue".equals(root)) return PermissionService.hasAccess(PermissionService.Rank.ROGUE);
        if ("elite".equals(root)) return PermissionService.hasAccess(PermissionService.Rank.ELITE);
        if ("alpha".equals(root)) return PermissionService.hasAccess(PermissionService.Rank.ALPHA);
        if ("alphaplus".equals(root)) return PermissionService.hasAccess(PermissionService.Rank.ALPHA_PLUS);
        return false;
    }

    private static boolean isFounderOnlyGardenRoute(String route) {
        return "prestigecoingive".equals(route) || "prestigecointake".equals(route)
                || "prestigecoinreset".equals(route) || "prestigereset".equals(route)
                || "admin".equals(route) || "multiplier".equals(route)
                || "coinsgive".equals(route) || "takecoins".equals(route)
                || "coinsreset".equals(route) || "give".equals(route);
    }

    private static boolean isStaff(PermissionService.Rank rank) {
        return rank == PermissionService.Rank.T_MOD || rank == PermissionService.Rank.MOD
                || rank == PermissionService.Rank.SR_MOD || rank == PermissionService.Rank.ADMIN
                || rank == PermissionService.Rank.SR_ADMIN || rank == PermissionService.Rank.PLAT_ADMIN
                || rank == PermissionService.Rank.MANAGER || rank == PermissionService.Rank.DEVELOPER
                || rank == PermissionService.Rank.OWNER || rank == PermissionService.Rank.FOUNDER
                || rank == PermissionService.Rank.MANAGEMENT;
    }

    private static boolean atLeast(PermissionService.Rank actual, PermissionService.Rank required) {
        switch (required) {
            case MOD: return actual == PermissionService.Rank.MOD || atLeast(actual, PermissionService.Rank.SR_MOD);
            case SR_MOD: return actual == PermissionService.Rank.SR_MOD || atLeast(actual, PermissionService.Rank.ADMIN);
            case ADMIN: return actual == PermissionService.Rank.ADMIN || atLeast(actual, PermissionService.Rank.SR_ADMIN);
            case SR_ADMIN: return actual == PermissionService.Rank.SR_ADMIN || atLeast(actual, PermissionService.Rank.PLAT_ADMIN);
            case PLAT_ADMIN: return actual == PermissionService.Rank.PLAT_ADMIN || actual == PermissionService.Rank.MANAGEMENT
                    || atLeast(actual, PermissionService.Rank.MANAGER);
            case MANAGER: return actual == PermissionService.Rank.MANAGER;
            case FOUNDER: return actual == PermissionService.Rank.FOUNDER;
            default: return false;
        }
    }
}
