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
        SCOUT,
        ROGUE,
        ELITE,
        ALPHA,
        ALPHA_PLUS,
        THE_NIBBLER,
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

    private static volatile Set<Rank> verifiedOnlineRanks = Collections.singleton(Rank.PUBLIC);
    private static volatile boolean onlineRankVerified = false;
    private static volatile Set<String> verifiedModEntitlements = Collections.emptySet();
    private static volatile java.util.Map<String, Boolean> verifiedModPermissions = Collections.emptyMap();
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
            clearVerifiedOnlineRank();
            return;
        }
        setVerifiedOnlineRanks(Collections.singleton(rank));
    }

    /** Sets the complete, authenticated set of ranks assigned to the signed-in account. */
    public static void setVerifiedOnlineRanks(Set<Rank> ranks) {
        if (ranks == null || ranks.isEmpty()) {
            verifiedOnlineRanks = Collections.singleton(Rank.PUBLIC);
            onlineRankVerified = false;
            return;
        }
        Set<Rank> safe = new HashSet<Rank>(ranks);
        safe.remove(null);
        if (safe.isEmpty()) {
            verifiedOnlineRanks = Collections.singleton(Rank.PUBLIC);
            onlineRankVerified = false;
            return;
        }
        verifiedOnlineRanks = Collections.unmodifiableSet(safe);
        onlineRankVerified = true;
    }

    public static Set<Rank> getVerifiedOnlineRanks() {
        return verifiedOnlineRanks;
    }

    public static void clearVerifiedOnlineRank() {
        verifiedOnlineRanks = Collections.singleton(Rank.PUBLIC);
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
        java.util.Map<String, Boolean> permissions = new java.util.HashMap<String, Boolean>();
        for (String modId : modIds) {
            if (modId != null && !modId.trim().isEmpty()) permissions.put(modId, Boolean.TRUE);
        }
        verifiedModPermissions = Collections.unmodifiableMap(permissions);
        modEntitlementsVerified = true;
    }

    /** Installs the complete authenticated per-mod permission result, including explicit denials. */
    public static void setVerifiedModPermissions(java.util.Map<String, Boolean> permissions) {
        if (permissions == null) {
            clearVerifiedModEntitlements();
            return;
        }
        java.util.Map<String, Boolean> safe = new java.util.HashMap<String, Boolean>();
        Set<String> allowed = new HashSet<String>();
        for (java.util.Map.Entry<String, Boolean> entry : permissions.entrySet()) {
            String modId = entry.getKey();
            if (modId == null || modId.trim().isEmpty()) continue;
            boolean enabled = Boolean.TRUE.equals(entry.getValue());
            safe.put(modId, enabled);
            if (enabled) allowed.add(modId);
        }
        verifiedModPermissions = Collections.unmodifiableMap(safe);
        verifiedModEntitlements = Collections.unmodifiableSet(allowed);
        modEntitlementsVerified = true;
    }

    public static void clearVerifiedModEntitlements() {
        verifiedModEntitlements = Collections.emptySet();
        verifiedModPermissions = Collections.emptyMap();
        modEntitlementsVerified = false;
    }

    /** Checks one stable database mod_id, not a guessed rank hierarchy. */
    public static boolean hasModAccess(String modId) {
        if (modId == null || modId.trim().isEmpty()) return false;
        if (isOfflineTestSession()) return true;
        Boolean explicit = verifiedModPermissions.get(modId);
        if (explicit != null) return explicit.booleanValue();
        return modEntitlementsVerified && verifiedModEntitlements.contains(modId);
    }

    public static boolean hasExplicitModPermission(String modId) {
        return modId != null && verifiedModPermissions.containsKey(modId);
    }

    public static boolean areModEntitlementsVerified() {
        return modEntitlementsVerified;
    }

    public static Rank getEffectiveRank() {
        if (isOfflineTestSession()) {
            return Rank.FOUNDER;
        }
        if (!onlineRankVerified || verifiedOnlineRanks.isEmpty()) return Rank.PUBLIC;
        Rank strongest = Rank.PUBLIC;
        int strongestPriority = -1;
        for (Rank rank : verifiedOnlineRanks) {
            int priority = rankPriority(rank);
            if (priority > strongestPriority) {
                strongestPriority = priority;
                strongest = rank;
            }
        }
        return strongest;
    }

    public static boolean hasAccess(Rank required) {
        if (required == null) return false;
        Set<Rank> ranks = isOfflineTestSession()
                ? Collections.singleton(Rank.FOUNDER)
                : (onlineRankVerified ? verifiedOnlineRanks : Collections.singleton(Rank.PUBLIC));
        if (required == Rank.FOUNDER) return ranks.contains(Rank.FOUNDER);
        if (ranks.contains(Rank.FOUNDER) || ranks.contains(Rank.OWNER)) return true;
        if (required == Rank.PUBLIC) return true;
        for (Rank actual : ranks) {
            if (actual == required) return true;
            if (actual == Rank.BETA || actual == Rank.DONATOR) continue;
            int actualLevel = staffLevel(actual);
            int requiredLevel = staffLevel(required);
            if (actualLevel >= 0 && requiredLevel >= 0 && actualLevel >= requiredLevel) return true;
        }
        return false;
    }

    /** Staff-only inheritance order. Supporter ranks never inherit staff access. */
    private static int staffLevel(Rank rank) {
        switch (rank) {
            case T_MOD: return 1;
            case MOD: return 2;
            case SR_MOD: return 3;
            case ADMIN: return 4;
            case SR_ADMIN: return 5;
            case PLAT_ADMIN:
            case MANAGEMENT: return 6; // Legacy key maps to Platform Admin.
            case MANAGER: return 7;
            case OWNER: return 8;
            case FOUNDER: return 9;
            default: return -1;
        }
    }

    /** Canonical Alpha Community ordering: higher value means higher displayed rank. */
    private static int rankPriority(Rank rank) {
        switch (rank) {
            case FOUNDER: return 17;
            case OWNER: return 16;
            case MANAGER: return 15;
            case PLAT_ADMIN:
            case MANAGEMENT: return 14;
            case SR_ADMIN: return 13;
            case ADMIN: return 12;
            case SR_MOD: return 11;
            case MOD: return 10;
            case T_MOD: return 9;
            case THE_NIBBLER: return 8;
            case BETA: return 7; // Beta Tester ranks immediately below The Nibbler.
            case ALPHA_PLUS: return 6;
            case ALPHA: return 5;
            case ELITE: return 4;
            case ROGUE: return 3;
            case SCOUT: return 2;
            case DONATOR: return 1; // Legacy compatibility only.
            case PUBLIC: return 0; // Member.
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
        return getEffectiveRank().name();
    }
}