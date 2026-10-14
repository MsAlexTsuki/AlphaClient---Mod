package com.alpharip.staffmod.permissions;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

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
        return actual.ordinal() >= required.ordinal();
    }

    public static boolean isOnlineRankVerified() {
        return onlineRankVerified;
    }

    public static String getStatusText() {
        if (isOfflineTestSession()) {
            return "FOUNDER OFFLINE TEST";
        }
        if (!onlineRankVerified) {
            return "COMMUNITY RANK NOT VERIFIED";
        }
        return verifiedOnlineRank.name();
    }
}