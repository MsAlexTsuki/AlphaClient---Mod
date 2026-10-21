package com.alpharip.staffmod.permissions;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Central client-side permission gate.
 *
 * Online community ranks must be supplied by the authenticated Alpha
 * community integration. Until that integration is configured, online
 * sessions intentionally receive only PUBLIC access.
 *
 * Offline test access is a local development convenience, not proof of
 * a production community rank and must never be accepted by a server.
 */
public final class PermissionService {
    public enum Rank {
        PUBLIC,
        BETA,
        DONATOR,
        T_MOD,
        MOD,
        SR_MOD,
        ADMIN,
        SR_ADMIN,
        PLAT_ADMIN,
        MANAGER,
        MANAGEMENT,
        OWNER,
        FOUNDER
    }

    public enum Mode {
        ONLINE_UNVERIFIED,
        OFFLINE_TEST
    }

    private static volatile Rank verifiedOnlineRank = Rank.PUBLIC;
    private static volatile boolean onlineRankVerified = false;
    private static volatile Set<String> verifiedModEntitlements = Collections.emptySet();
    private static volatile boolean modEntitlementsVerified = false;

    private PermissionService() {
    }

    public static Mode getMode() {
        return isOfflineTestSession() ? Mode.OFFLINE_TEST : Mode.ONLINE_UNVERIFIED;
    }

    public static boolean isOfflineTestSession() {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc == null) {
                return false;
            }
            Session session = mc.getSession();
            if (session == null) {
                return false;
            }
            String token = session.getToken();
            return token == null || token.trim().isEmpty() || "0".equals(token);
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * Called only by a future trusted community-rank adapter after it has
     * verified the signed-in user's rank with the existing Alpha community.
     */
    public static void setVerifiedOnlineRank(Rank rank) {
        if (rank == null) {
            verifiedOnlineRank = Rank.PUBLIC;
            onlineRankVerified = false;
            return;
        }
        verifiedOnlineRank = rank;
        onlineRankVerified = true;
    }

    public static void clearVerifiedOnlineRank() {
        verifiedOnlineRank = Rank.PUBLIC;
        onlineRankVerified = false;
        clearVerifiedModEntitlements();
    }

    /**
     * Supplies the exact mod IDs returned by the authenticated Alpha permission API.
     * Call only after validating the response for the signed-in Alpha account.
     * Empty or missing responses fail closed for every non-public mod.
     */
    public static void setVerifiedModEntitlements(Set<String> modIds) {
        if (modIds == null) {
            clearVerifiedModEntitlements();
            return;
        }
        verifiedModEntitlements = Collections.unmodifiableSet(new HashSet<String>(modIds));
        modEntitlementsVerified = true;
    }

    public static void clearVerifiedModEntitlements() {
        verifiedModEntitlements = Collections.emptySet();
        modEntitlementsVerified = false;
    }

    /** Checks one stable database mod_id, not a guessed rank hierarchy. */
    public static boolean hasModAccess(String modId) {
        if (modId == null || modId.trim().isEmpty()) return false;
        if (isOfflineTestSession()) return true;
        if (!modEntitlementsVerified) return false;
        return verifiedModEntitlements.contains(modId);
    }

    public static boolean areModEntitlementsVerified() {
        return modEntitlementsVerified;
    }

    public static Rank getEffectiveRank() {
        if (isOfflineTestSession()) {
            return Rank.FOUNDER;
        }
        return onlineRankVerified ? verifiedOnlineRank : Rank.PUBLIC;
    }

    public static boolean hasAccess(Rank required) {
        if (required == null) {
            return false;
        }
        Rank actual = getEffectiveRank();
        if (actual == Rank.FOUNDER) return true;
        if (required == Rank.PUBLIC) return true;

        // Beta and Donator are separate entitlements, except for staff and Founder.
        if (actual == Rank.BETA || actual == Rank.DONATOR) return actual == required;

        // Staff ranks inherit categories below their own rank.
        int actualLevel = staffLevel(actual);
        int requiredLevel = staffLevel(required);
        return actualLevel >= 0 && requiredLevel >= 0 && actualLevel >= requiredLevel;
    }

    private static int staffLevel(Rank rank) {
        switch (rank) {
            case T_MOD: return 1;
            case MOD: return 2;
            case SR_MOD: return 3;
            case ADMIN: return 4;
            case SR_ADMIN: return 5;
            case PLAT_ADMIN: return 6;
            case MANAGEMENT: return 6; // Legacy rank key; equivalent to Platform Admin.
            case MANAGER: return 7;
            case OWNER: return 8;
            case FOUNDER: return 9;
            default: return -1;
        }
    }

    public static boolean isOnlineRankVerified() {
        return onlineRankVerified;
    }

    public static String getStatusText() {
        if (isOfflineTestSession()) {
            return "FOUNDER OFFLINE TEST";
        }
        if (!modEntitlementsVerified) {
            return "MOD PERMISSIONS NOT VERIFIED";
        }
        if (!onlineRankVerified) {
            return "COMMUNITY RANK NOT VERIFIED";
        }
        return verifiedOnlineRank.name();
    }
}