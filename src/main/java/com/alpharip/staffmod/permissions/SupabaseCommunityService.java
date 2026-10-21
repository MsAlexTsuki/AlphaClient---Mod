package com.alpharip.staffmod.permissions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import java.util.UUID;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Authenticates against Alpha's Supabase community and keeps the verified
 * rank and module permissions synchronized while Minecraft is running.
 *
 * The password is never saved. A Supabase refresh token is stored in the
 * local AlphaClient config so the player does not need to sign in every launch.
 */
public final class SupabaseCommunityService {
    private static final String SUPABASE_URL = "https://lescvpixttmmbnaluidd.supabase.co";
    private static final String PUBLISHABLE_KEY = "sb_publishable_DEeIl6IL4mPXiEx6SaJ3EQ_OvNhEPP8";
    private static final long SYNC_INTERVAL_SECONDS = 15L;
    private static final long REFRESH_EARLY_SECONDS = 90L;

    private static volatile String accessToken;
    private static volatile String refreshToken;
    private static volatile String signedInUserId;
    private static volatile long accessTokenExpiresAt;
    private static volatile String signedInEmail;
    private static volatile String signedInRankLabel;
    private static volatile String signedInRankKey;
    private static volatile boolean signedInFounder;
    private static volatile boolean restoringSession;
    private static volatile String sessionStatus = "Alpha Account has not been signed in on this installation.";
    private static ScheduledExecutorService syncExecutor;

    private SupabaseCommunityService() {
    }

    /** Starts restore and live rank/permission synchronization once per client process. */
    public static synchronized void start() {
        if (syncExecutor != null && !syncExecutor.isShutdown()) return;
        syncExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "AlphaClient-CommunitySync");
                thread.setDaemon(true);
                return thread;
            }
        });
        syncExecutor.execute(new Runnable() {
            @Override public void run() {
                restoringSession = true;
                try {
                    loadSavedRefreshToken();
                    if (refreshToken != null) {
                        sessionStatus = "Restoring saved Alpha Account session...";
                        refreshSessionToken();
                        synchronizeVerifiedAccount();
                        sessionStatus = "Alpha Account session restored.";
                    } else {
                        sessionStatus = "No saved Alpha Account session. Sign in once to save this account.";
                    }
                } catch (Exception exception) {
                    sessionStatus = "Session restore failed: " + (exception.getMessage() == null ? "unknown error" : exception.getMessage());
                    System.err.println("[AlphaClient] Could not restore Alpha Account session: "
                            + exception.getMessage());
                    // Keep the refresh token for a later retry if this was a network failure.
                    if (accessToken == null) PermissionService.clearVerifiedOnlineRank();
                } finally {
                    restoringSession = false;
                }
            }
        });
        syncExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override public void run() {
                if (refreshToken == null) return;
                try {
                    synchronized (SupabaseCommunityService.class) {
                        if (accessToken == null || accessTokenExpiresAt <=
                                (System.currentTimeMillis() / 1000L) + REFRESH_EARLY_SECONDS) {
                            refreshSessionToken();
                        }
                        synchronizeVerifiedAccount();
                    }
                } catch (Exception exception) {
                    System.err.println("[AlphaClient] Community sync failed; will retry: "
                            + exception.getMessage());
                    if (accessToken == null) PermissionService.clearVerifiedOnlineRank();
                }
            }
        }, SYNC_INTERVAL_SECONDS, SYNC_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    public static boolean isRestoringSession() {
        return restoringSession;
    }

    public static String getSessionStatus() {
        return sessionStatus;
    }

    public static synchronized void signIn(String email, String password) throws Exception {
        if (restoringSession) {
            throw new IllegalStateException("Restoring your saved Alpha Account session. Please wait a moment.");
        }
        if (accessToken != null) {
            throw new IllegalStateException("Already signed in to an Alpha community account. Sign out before switching accounts.");
        }
        clearInMemorySession();
        PermissionService.clearVerifiedOnlineRank();
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Enter your Alpha community email and password.");
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("email", email.trim());
        payload.addProperty("password", password);
        JsonObject auth = requestJson("POST", "/auth/v1/token?grant_type=password", null, payload.toString());

        String token = getString(auth, "access_token");
        String nextRefreshToken = getString(auth, "refresh_token");
        JsonObject user = auth.has("user") && auth.get("user").isJsonObject()
                ? auth.getAsJsonObject("user") : null;
        String userId = user == null ? null : getString(user, "id");
        if (token == null || nextRefreshToken == null || userId == null) {
            throw new IllegalStateException("Supabase did not return a complete authenticated session.");
        }

        accessToken = token;
        refreshToken = nextRefreshToken;
        signedInUserId = userId;
        accessTokenExpiresAt = (System.currentTimeMillis() / 1000L) + Math.max(60L, getLong(auth, "expires_in", 3600L));
        signedInEmail = email.trim();
        sessionStatus = "Signed in. Saving Alpha Account session...";
        persistRefreshToken();
        synchronizeVerifiedAccount();
        sessionStatus = "Signed in as " + signedInEmail + ". Rank and permissions are synchronized.";
        linkCurrentMinecraftUuid(accessToken);
    }

    /**
     * Restores a session by exchanging the saved refresh token for a fresh access token.
     * Refresh-token rotation is persisted immediately after each successful exchange.
     */
    private static synchronized void refreshSessionToken() throws Exception {
        if (refreshToken == null || refreshToken.trim().isEmpty()) return;
        JsonObject payload = new JsonObject();
        payload.addProperty("refresh_token", refreshToken);
        JsonObject auth = requestJson("POST", "/auth/v1/token?grant_type=refresh_token", null, payload.toString());

        String nextAccessToken = getString(auth, "access_token");
        String nextRefreshToken = getString(auth, "refresh_token");
        JsonObject user = auth.has("user") && auth.get("user").isJsonObject()
                ? auth.getAsJsonObject("user") : null;
        String userId = user == null ? signedInUserId : getString(user, "id");
        String email = user == null ? signedInEmail : getString(user, "email");
        if (nextAccessToken == null || nextRefreshToken == null || userId == null) {
            throw new IllegalStateException("Supabase did not return a refreshed Alpha Account session.");
        }

        accessToken = nextAccessToken;
        refreshToken = nextRefreshToken;
        signedInUserId = userId;
        if (email != null) signedInEmail = email;
        accessTokenExpiresAt = (System.currentTimeMillis() / 1000L) + Math.max(60L, getLong(auth, "expires_in", 3600L));
        persistRefreshToken();
    }

    /** Fetches the current rank and effective per-module access from authenticated RPCs. */
    private static synchronized void synchronizeVerifiedAccount() throws Exception {
        String token = accessToken;
        String userId = signedInUserId;
        if (token == null || userId == null) return;

        String query = "/rest/v1/profiles?select=rank_key,is_founder&id=eq."
                + java.net.URLEncoder.encode(userId, "UTF-8") + "&limit=1";
        JsonArray profiles = requestArray("GET", query, token);
        if (profiles.size() == 0 || !profiles.get(0).isJsonObject()) {
            PermissionService.clearVerifiedOnlineRank();
            throw new IllegalStateException("No Alpha community profile is linked to this account.");
        }

        JsonObject profile = profiles.get(0).getAsJsonObject();
        String rankKey = getString(profile, "rank_key");
        boolean founder = getBoolean(profile, "is_founder");
        PermissionService.Rank rank = mapRank(rankKey, founder);

        // The profile's current rank is authoritative; never trust a cached UI badge.
        signedInRankKey = rankKey;
        signedInFounder = founder;
        signedInRankLabel = formatRankLabel(rankKey, founder);
        PermissionService.setVerifiedOnlineRank(rank);

        JsonArray permissionRows = requestArray("POST", "/rest/v1/rpc/get_my_client_mod_permissions", token, "{}");
        Map<String, Boolean> modulePermissions = new HashMap<String, Boolean>();
        for (JsonElement element : permissionRows) {
            if (!element.isJsonObject()) continue;
            JsonObject row = element.getAsJsonObject();
            String modId = getString(row, "mod_id");
            if (modId != null) modulePermissions.put(modId, getBoolean(row, "allowed"));
        }
        PermissionService.setVerifiedModPermissions(modulePermissions);
    }

    /**
     * Links this authenticated community profile to the current online Minecraft UUID
     * for public rank display only. This mapping must never grant client/server permissions.
     */
    private static void linkCurrentMinecraftUuid(String token) {
        if (PermissionService.isOfflineTestSession()) return;
        try {
            Minecraft minecraft = Minecraft.getMinecraft();
            if (minecraft == null || minecraft.getSession() == null
                    || minecraft.getSession().getProfile() == null) return;
            UUID uuid = minecraft.getSession().getProfile().getId();
            if (uuid == null) return;
            JsonObject payload = new JsonObject();
            payload.addProperty("target_uuid", uuid.toString());
            requestJson("POST", "/rest/v1/rpc/link_minecraft_uuid", token, payload.toString());
        } catch (Exception exception) {
            System.err.println("[AlphaClient] Could not link Minecraft UUID for public rank display: "
                    + exception.getMessage());
        }
    }

    public static synchronized void signOut() {
        String token = accessToken;
        clearInMemorySession();
        sessionStatus = "Signed out. This Alpha Account session was removed from this installation.";
        deleteSavedSession();
        PermissionService.clearVerifiedOnlineRank();
        if (token != null) {
            try {
                requestJson("POST", "/auth/v1/logout", token, "{}");
            } catch (Exception ignored) {
                // Local sign-out remains effective if the network is unavailable.
            }
        }
    }

    private static void clearInMemorySession() {
        accessToken = null;
        refreshToken = null;
        signedInUserId = null;
        accessTokenExpiresAt = 0L;
        signedInEmail = null;
        signedInRankLabel = null;
        signedInRankKey = null;
        signedInFounder = false;
    }

    private static File getSessionFile() {
        File base;
        try {
            Minecraft minecraft = Minecraft.getMinecraft();
            base = minecraft != null && minecraft.mcDataDir != null
                    ? minecraft.mcDataDir : new File(System.getProperty("user.dir", "."));
        } catch (Throwable ignored) {
            base = new File(System.getProperty("user.dir", "."));
        }
        return new File(new File(new File(base, "config"), "alphastaffmod"), "community_session.json");
    }

    private static void loadSavedRefreshToken() throws Exception {
        File file = getSessionFile();
        if (!file.isFile()) return;
        FileReader reader = new FileReader(file);
        try {
            JsonElement parsed = new JsonParser().parse(reader);
            if (!parsed.isJsonObject()) return;
            JsonObject saved = parsed.getAsJsonObject();
            refreshToken = getString(saved, "refresh_token");
            signedInUserId = getString(saved, "user_id");
            signedInEmail = getString(saved, "email");
        } finally {
            reader.close();
        }
    }

    private static synchronized void persistRefreshToken() {
        if (refreshToken == null || signedInUserId == null) return;
        File file = getSessionFile();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            System.err.println("[AlphaClient] Could not create Alpha Account session directory.");
            return;
        }
        JsonObject saved = new JsonObject();
        saved.addProperty("refresh_token", refreshToken);
        saved.addProperty("user_id", signedInUserId);
        if (signedInEmail != null) saved.addProperty("email", signedInEmail);
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(saved.toString());
            writer.flush();
        } catch (Exception exception) {
            System.err.println("[AlphaClient] Could not save Alpha Account session: " + exception.getMessage());
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (Exception ignored) { }
            }
        }
    }

    private static void deleteSavedSession() {
        File file = getSessionFile();
        if (file.isFile() && !file.delete()) {
            System.err.println("[AlphaClient] Could not delete saved Alpha Account session file.");
        }
    }

    public static boolean isSignedIn() { return accessToken != null; }
    public static String getSignedInEmail() { return signedInEmail; }
    public static String getSignedInRankLabel() { return signedInRankLabel == null ? "No Rank" : signedInRankLabel; }

    /** Returns the badge rank for the currently authenticated Alpha account, if any. */
    public static com.alpharip.staffmod.rankviewer.AlphaRank getSignedInDisplayRank() {
        if (accessToken == null) return null;
        if (signedInFounder || "founder".equalsIgnoreCase(signedInRankKey)) {
            return com.alpharip.staffmod.rankviewer.AlphaRank.FOUNDER;
        }
        return com.alpharip.staffmod.rankviewer.AlphaRank.fromString(signedInRankKey);
    }

    private static String formatRankLabel(String rankKey, boolean founder) {
        if (founder || "founder".equalsIgnoreCase(rankKey)) return "Founder";
        if (rankKey == null || rankKey.trim().isEmpty() || "public".equalsIgnoreCase(rankKey)) return "Member";
        String[] parts = rankKey.trim().toLowerCase(java.util.Locale.ROOT).split("[-_ ]+");
        StringBuilder label = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (label.length() > 0) label.append(' ');
            label.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return label.toString();
    }

    private static PermissionService.Rank mapRank(String rankKey, boolean founder) {
        if (founder || "founder".equalsIgnoreCase(rankKey)) return PermissionService.Rank.FOUNDER;
        if (rankKey == null) return PermissionService.Rank.PUBLIC;
        String normalized = rankKey.trim().toLowerCase(java.util.Locale.ROOT).replace('-', '_').replace(' ', '_');
        switch (normalized) {
            case "beta": return PermissionService.Rank.BETA;
            case "alpha":
            case "alpha_plus":
            case "donator": return PermissionService.Rank.DONATOR;
            case "t_mod":
            case "trial_mod":
            case "trial_moderator":
            case "tmod": return PermissionService.Rank.T_MOD;
            case "mod":
            case "moderator": return PermissionService.Rank.MOD;
            case "sr_mod":
            case "senior_mod":
            case "senior_moderator":
            case "smod": return PermissionService.Rank.SR_MOD;
            case "admin": return PermissionService.Rank.ADMIN;
            case "sr_admin":
            case "senior_admin":
            case "sadmin": return PermissionService.Rank.SR_ADMIN;
            case "management":
            case "plat_admin":
            case "platform_admin": return PermissionService.Rank.PLAT_ADMIN;
            case "manager": return PermissionService.Rank.MANAGER;
            case "owner": return PermissionService.Rank.OWNER;
            default: return PermissionService.Rank.PUBLIC;
        }
    }

    private static JsonObject requestJson(String method, String path, String bearer, String body) throws Exception {
        HttpURLConnection connection = open(method, path, bearer);
        connection.setDoOutput(true);
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Content-Length", String.valueOf(bytes.length));
        OutputStream output = connection.getOutputStream();
        try { output.write(bytes); } finally { output.close(); }
        String response = readResponse(connection);
        JsonElement parsed = new JsonParser().parse(response);
        if (!parsed.isJsonObject()) throw new IllegalStateException("Unexpected response from Alpha community service.");
        return parsed.getAsJsonObject();
    }

    private static JsonArray requestArray(String method, String path, String bearer) throws Exception {
        HttpURLConnection connection = open(method, path, bearer);
        String response = readResponse(connection);
        JsonElement parsed = new JsonParser().parse(response);
        if (!parsed.isJsonArray()) throw new IllegalStateException("Unexpected array response from Alpha community service.");
        return parsed.getAsJsonArray();
    }

    private static JsonArray requestArray(String method, String path, String bearer, String body) throws Exception {
        HttpURLConnection connection = open(method, path, bearer);
        connection.setDoOutput(true);
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Content-Length", String.valueOf(bytes.length));
        OutputStream output = connection.getOutputStream();
        try { output.write(bytes); } finally { output.close(); }
        String response = readResponse(connection);
        JsonElement parsed = new JsonParser().parse(response);
        if (!parsed.isJsonArray()) throw new IllegalStateException("Unexpected array response from Alpha community service.");
        return parsed.getAsJsonArray();
    }

    private static HttpURLConnection open(String method, String path, String bearer) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(SUPABASE_URL + path).openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(8000);
        connection.setRequestProperty("apikey", PUBLISHABLE_KEY);
        connection.setRequestProperty("Accept", "application/json");
        if (bearer != null && !bearer.trim().isEmpty()) {
            connection.setRequestProperty("Authorization", "Bearer " + bearer);
        }
        return connection;
    }

    private static String readResponse(HttpURLConnection connection) throws Exception {
        int code = connection.getResponseCode();
        InputStream stream = code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream();
        StringBuilder text = new StringBuilder();
        if (stream != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            try {
                String line;
                while ((line = reader.readLine()) != null) text.append(line);
            } finally {
                reader.close();
            }
        }
        connection.disconnect();
        if (code < 200 || code >= 300) {
            String message = "Alpha community request failed (HTTP " + code + ").";
            try {
                JsonElement parsed = new JsonParser().parse(text.toString());
                if (parsed.isJsonObject()) {
                    String detail = getString(parsed.getAsJsonObject(), "msg");
                    if (detail == null) detail = getString(parsed.getAsJsonObject(), "message");
                    if (detail != null && !detail.trim().isEmpty()) message = detail;
                }
            } catch (Exception ignored) { }
            throw new IllegalStateException(message);
        }
        return text.toString();
    }

    private static String getString(JsonObject object, String key) {
        try {
            JsonElement value = object.get(key);
            return value == null || value.isJsonNull() ? null : value.getAsString();
        } catch (Exception ignored) { return null; }
    }

    private static boolean getBoolean(JsonObject object, String key) {
        try {
            JsonElement value = object.get(key);
            return value != null && !value.isJsonNull() && value.getAsBoolean();
        } catch (Exception ignored) { return false; }
    }

    private static long getLong(JsonObject object, String key, long fallback) {
        try {
            JsonElement value = object.get(key);
            return value == null || value.isJsonNull() ? fallback : value.getAsLong();
        } catch (Exception ignored) { return fallback; }
    }
}
