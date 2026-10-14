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
        STAFF,
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
        if (actual == Rank.FOUNDER) {
            return true;
        }
        if (required == Rank.PUBLIC) {
            return true;
        }
        if (actual == Rank.STAFF) {
            return true;
        }
        // Beta and Donator are separate entitlements, not a single rank ladder.
        return actual == required;
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