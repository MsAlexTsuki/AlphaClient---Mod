package com.alpharip.staffmod.permissions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Authenticates against the existing Alpha community Supabase project.
 * Access tokens remain in memory and are never written to disk.
 */
public final class SupabaseCommunityService {
    private static final String SUPABASE_URL = "https://lescvpixttmmbnaluidd.supabase.co";
    private static final String PUBLISHABLE_KEY = "sb_publishable_DEeIl6IL4mPXiEx6SaJ3EQ_OvNhEPP8";

    private static volatile String accessToken;
    private static volatile String signedInEmail;
    private static volatile String signedInRankLabel;

    private SupabaseCommunityService() {
    }

    public static synchronized void signIn(String email, String password) throws Exception {
        accessToken = null;
        signedInEmail = null;
        signedInRankLabel = null;
        PermissionService.clearVerifiedOnlineRank();
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Enter your Alpha community email and password.");
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("email", email.trim());
        payload.addProperty("password", password);

        JsonObject auth = requestJson(
                "POST",
                "/auth/v1/token?grant_type=password",
                null,
                payload.toString()
        );

        String token = getString(auth, "access_token");
        JsonObject user = auth.has("user") && auth.get("user").isJsonObject()
                ? auth.getAsJsonObject("user") : null;
        String userId = user == null ? null : getString(user, "id");
        if (token == null || userId == null) {
            throw new IllegalStateException("Supabase did not return an authenticated user session.");
        }

        // Resolve the authenticated account's community profile, never a client-supplied username.
        String query = "/rest/v1/profiles?select=rank_key,is_founder&id=eq."
                + URLEncoder.encode(userId, "UTF-8") + "&limit=1";
        JsonArray profiles = requestArray("GET", query, token);
        if (profiles.size() == 0 || !profiles.get(0).isJsonObject()) {
            throw new IllegalStateException("No Alpha community profile is linked to this account.");
        }

        JsonObject profile = profiles.get(0).getAsJsonObject();
        String rankKey = getString(profile, "rank_key");
        boolean founder = getBoolean(profile, "is_founder");
        PermissionService.Rank rank = mapRank(rankKey, founder);

        accessToken = token;
        signedInEmail = email.trim();
        signedInRankLabel = formatRankLabel(rankKey, founder);
        PermissionService.setVerifiedOnlineRank(rank);
    }

    public static synchronized void signOut() {
        String token = accessToken;
        accessToken = null;
        signedInEmail = null;
        signedInRankLabel = null;
        PermissionService.clearVerifiedOnlineRank();
        if (token != null) {
            try {
                requestJson("POST", "/auth/v1/logout", token, "{}");
            } catch (Exception ignored) {
                // Local sign-out remains effective if the network is unavailable.
            }
        }
    }

    public static boolean isSignedIn() {
        return accessToken != null;
    }

    public static String getSignedInEmail() {
        return signedInEmail;
    }

    public static String getSignedInRankLabel() {
        return signedInRankLabel == null ? "No Rank" : signedInRankLabel;
    }

    private static String formatRankLabel(String rankKey, boolean founder) {
        if (founder || "founder".equalsIgnoreCase(rankKey)) return "Founder";
        if (rankKey == null || rankKey.trim().isEmpty() || "public".equalsIgnoreCase(rankKey)) return "Member";
        String[] parts = rankKey.trim().toLowerCase(java.util.Locale.ROOT).split("[_\\\\- ]+");
        StringBuilder label = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (label.length() > 0) label.append(' ');
            label.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return label.toString();
    }

    private static PermissionService.Rank mapRank(String rankKey, boolean founder) {
        if (founder || "founder".equalsIgnoreCase(rankKey)) {
            return PermissionService.Rank.FOUNDER;
        }
        if ("beta".equalsIgnoreCase(rankKey)) {
            return PermissionService.Rank.BETA;
        }
        if ("alpha".equalsIgnoreCase(rankKey) || "alpha_plus".equalsIgnoreCase(rankKey)) {
            return PermissionService.Rank.DONATOR;
        }
        if ("owner".equalsIgnoreCase(rankKey)
                || "management".equalsIgnoreCase(rankKey)
                || "senior_admin".equalsIgnoreCase(rankKey)
                || "admin".equalsIgnoreCase(rankKey)
                || "senior_moderator".equalsIgnoreCase(rankKey)
                || "moderator".equalsIgnoreCase(rankKey)
                || "trial_moderator".equalsIgnoreCase(rankKey)) {
            return PermissionService.Rank.STAFF;
        }
        return PermissionService.Rank.PUBLIC;
    }

    private static JsonObject requestJson(String method, String path, String bearer, String body) throws Exception {
        HttpURLConnection connection = open(method, path, bearer);
        connection.setDoOutput(true);
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Content-Length", String.valueOf(bytes.length));
        OutputStream output = connection.getOutputStream();
        output.write(bytes);
        output.close();
        String response = readResponse(connection);
        JsonElement parsed = new JsonParser().parse(response);
        if (!parsed.isJsonObject()) {
            throw new IllegalStateException("Unexpected response from Alpha community service.");
        }
        return parsed.getAsJsonObject();
    }

    private static JsonArray requestArray(String method, String path, String bearer) throws Exception {
        HttpURLConnection connection = open(method, path, bearer);
        String response = readResponse(connection);
        JsonElement parsed = new JsonParser().parse(response);
        if (!parsed.isJsonArray()) {
            throw new IllegalStateException("Unexpected profile response from Alpha community service.");
        }
        return parsed.getAsJsonArray();
    }

    private static HttpURLConnection open(String method, String path, String bearer) throws Exception {
        URL url = new URL(SUPABASE_URL + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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
        InputStream stream = code >= 200 && code < 300
                ? connection.getInputStream() : connection.getErrorStream();
        StringBuilder text = new StringBuilder();
        if (stream != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line);
            }
            reader.close();
        }
        connection.disconnect();
        if (code < 200 || code >= 300) {
            String message = "Alpha community sign-in failed (HTTP " + code + ").";
            try {
                JsonElement parsed = new JsonParser().parse(text.toString());
                if (parsed.isJsonObject()) {
                    String detail = getString(parsed.getAsJsonObject(), "msg");
                    if (detail == null) detail = getString(parsed.getAsJsonObject(), "message");
                    if (detail != null && !detail.trim().isEmpty()) message = detail;
                }
            } catch (Exception ignored) {
            }
            throw new IllegalStateException(message);
        }
        return text.toString();
    }

    private static String getString(JsonObject object, String key) {
        try {
            JsonElement value = object.get(key);
            return value == null || value.isJsonNull() ? null : value.getAsString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean getBoolean(JsonObject object, String key) {
        try {
            JsonElement value = object.get(key);
            return value != null && !value.isJsonNull() && value.getAsBoolean();
        } catch (Exception ignored) {
            return false;
        }
    }
}