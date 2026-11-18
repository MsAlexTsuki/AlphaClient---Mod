// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.AlphaClans;

import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Authenticated Supabase-backed AlphaClans operations. */
public final class AlphaClanService {
    public static final AlphaClanService INSTANCE = new AlphaClanService();
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "AlphaClient-Clans"); t.setDaemon(true); return t;
    });
    private final AtomicBoolean loading = new AtomicBoolean(false);
    private final AtomicBoolean actionRunning = new AtomicBoolean(false);
    private final ScheduledExecutorService presenceWorker = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "AlphaClient-ClanPresence"); t.setDaemon(true); return t;
    });
    private volatile ScheduledFuture<?> presenceTask;
    private volatile Snapshot snapshot = new Snapshot();
    private volatile long lastRefresh;
    private volatile String realtimeClan;
    private volatile long lastPresenceHeartbeat;

    private AlphaClanService() {}
    public Snapshot getSnapshot() { return snapshot; }
    public void onSignedOut() {
        realtimeClan = null;
        stopPresenceHeartbeat();
        ClanRealtimeClient.INSTANCE.disconnect();
        Snapshot value = new Snapshot();
        value.status = "Sign in to your Alpha Account to use AlphaClans.";
        snapshot = value;
        lastRefresh = System.currentTimeMillis();
    }
    public void refreshIfStale() { if (System.currentTimeMillis() - lastRefresh > 5000L) refresh(); }

    public void refresh() {
        if (!SupabaseCommunityService.isSignedIn()) {
            stopPresenceHeartbeat();
            Snapshot value = new Snapshot();
            value.status = "Sign in to your Alpha Account to use AlphaClans.";
            snapshot = value;
            lastRefresh = System.currentTimeMillis();
            return;
        }
        if (actionRunning.get() || !loading.compareAndSet(false, true)) return;
        worker.execute(() -> {
            try {
                Snapshot value = load();
                snapshot = value;
                lastRefresh = System.currentTimeMillis();
                String id = value.clan == null ? null : str(value.clan, "id");
                if (id == null) {
                    stopPresenceHeartbeat();
                    realtimeClan = null;
                    ClanRealtimeClient.INSTANCE.disconnect();
                } else {
                    startPresenceHeartbeat();
                    if (!id.equals(realtimeClan)) { realtimeClan = id; ClanRealtimeClient.INSTANCE.subscribe(id); }
                }
            } catch (Exception e) {
                Snapshot value = snapshot;
                value.status = message(e);
            } finally { loading.set(false); }
        });
    }

    private Snapshot load() throws Exception {
        Snapshot s = new Snapshot();
        boolean presenceAvailable = true;
        if (!SupabaseCommunityService.isSignedIn()) { s.status = "Sign in to your Alpha Account to use AlphaClans."; return s; }
        String uid = SupabaseCommunityService.getSignedInUserId();
        if (uid == null) throw new IllegalStateException("Alpha profile not found.");
        JsonArray membership = SupabaseCommunityService.clanRequestArray("GET",
                "/rest/v1/clan_members?select=clan_id,profile_id,role&profile_id=eq." + enc(uid) + "&limit=1");
        if (membership.size() > 0) {
            JsonObject member = membership.get(0).getAsJsonObject();
            String clanId = str(member, "clan_id");
            JsonArray clans = SupabaseCommunityService.clanRequestArray("GET",
                    "/rest/v1/clans?select=id,name,tag,tag_color,description,visibility,owner_id&id=eq." + enc(clanId) + "&limit=1");
            if (clans.size() > 0) s.clan = clans.get(0).getAsJsonObject();
            try {
                heartbeatPresence();
                s.members = SupabaseCommunityService.clanRequestArray("GET",
                        "/rest/v1/clan_members?select=profile_id,role,joined_at,profiles(username,display_name),clan_presence(online,last_seen_at)&clan_id=eq."
                                + enc(clanId) + "&order=joined_at.asc");
            } catch (Exception presenceError) {
                presenceAvailable = false;
                s.members = SupabaseCommunityService.clanRequestArray("GET",
                        "/rest/v1/clan_members?select=profile_id,role,joined_at,profiles(username,display_name)&clan_id=eq."
                                + enc(clanId) + "&order=joined_at.asc");
            }
            s.messages = SupabaseCommunityService.clanRequestArray("GET",
                    "/rest/v1/clan_messages?select=id,sender_id,body,created_at,profiles(username,display_name)&clan_id=eq." + enc(clanId) + "&order=created_at.desc&limit=100");
            s.requests = SupabaseCommunityService.clanRequestArray("GET",
                    "/rest/v1/clan_join_requests?select=id,profile_id,created_at,profiles(username,display_name)&clan_id=eq." + enc(clanId) + "&status=eq.pending&order=created_at.asc");
            s.owner = "owner".equals(str(member, "role"));
            s.officer = "officer".equals(str(member, "role"));
        }
        s.browser = SupabaseCommunityService.clanRequestArray("POST", "/rest/v1/rpc/get_clan_browser", "{}");
        if (s.clan != null) {
            String ownClanId = str(s.clan, "id");
            JsonArray available = new JsonArray();
            for (int i = 0; i < s.browser.size(); i++) {
                JsonObject candidate = s.browser.get(i).getAsJsonObject();
                if (!ownClanId.equals(str(candidate, "id"))) available.add(candidate);
            }
            s.browser = available;
        }
        s.invites = SupabaseCommunityService.clanRequestArray("GET",
                "/rest/v1/clan_invites?select=id,clan_id,expires_at,clans(name,tag)&target_profile_id=eq." + enc(uid)
                        + "&expires_at=gt." + enc(java.time.Instant.now().toString()) + "&order=created_at.desc");
        s.status = s.clan == null ? "Browse public and private clans or create one."
                : (presenceAvailable ? "Clan loaded." : "Clan loaded. Presence data is unavailable.");
        return s;
    }

    public void create(String name, String tag, boolean isPrivate) {
        action("Clan created.", () -> {
            validateClanIdentity(name, tag);
            JsonObject p = new JsonObject(); p.addProperty("p_name", name.trim()); p.addProperty("p_tag", tag.trim().toUpperCase(java.util.Locale.ROOT));
            p.addProperty("p_description", ""); p.addProperty("p_visibility", isPrivate ? "private" : "public");
            rpc("create_clan", p);
        });
    }
    public void join(String id) { action("Joined clan.", () -> rpcValue("join_public_clan", "p_clan_id", id)); }
    public void requestJoin(String id) { action("Join request sent.", () -> rpcValue("request_to_join_clan", "p_clan_id", id)); }
    public void invite(String username) { action("Invite sent.", () -> {
        JsonObject p = new JsonObject(); p.addProperty("p_clan_id", clanId()); p.addProperty("p_username", username); rpc("invite_clan_member", p);
    }); }
    public void acceptInvite(String id, boolean accept) { action(accept ? "Invite accepted." : "Invite declined.", () -> {
        JsonObject p = new JsonObject(); p.addProperty("p_invite_id", id); p.addProperty("p_accept", accept); rpc("respond_to_clan_invite", p);
    }); }
    public void respondRequest(String id, boolean accept) { action(accept ? "Join request approved." : "Join request declined.", () -> {
        JsonObject p = new JsonObject(); p.addProperty("p_request_id", id); p.addProperty("p_accept", accept); rpc("respond_to_clan_join_request", p);
    }); }
    public void sendMessage(String text) { action("Message sent.", () -> {
        if (text == null || text.trim().isEmpty() || text.length() > 256) throw new IllegalArgumentException("Clan messages must contain 1 to 256 characters.");
        JsonObject p = new JsonObject(); p.addProperty("clan_id", clanId()); p.addProperty("sender_id", SupabaseCommunityService.getSignedInUserId()); p.addProperty("body", text.trim());
        SupabaseCommunityService.clanRequestArray("POST", "/rest/v1/clan_messages?select=id", p.toString());
    }); }
    public void leave() { action("You left the clan.", () -> rpc("leave_clan", new JsonObject())); }
    public void kick(String id) { action("Member removed.", () -> rpcValue("remove_clan_member", "p_target_profile_id", id)); }
    public void setRole(String id, boolean officer) { action("Member role updated.", () -> {
        JsonObject p = new JsonObject(); p.addProperty("p_target_profile_id", id); p.addProperty("p_role", officer ? "officer" : "member"); rpc("set_clan_member_role", p);
    }); }
    public void transfer(String id) { action("Clan ownership transferred.", () -> rpcValue("transfer_clan_ownership", "p_target_profile_id", id)); }
    public void disband() { action("Clan disbanded.", () -> rpcValue("disband_clan", "p_clan_id", clanId())); }
    public void setTagColor(String color) { action("Clan tag color updated.", () -> {
        String[] allowed = {"black","dark_blue","dark_green","dark_aqua","dark_red","dark_purple","gold","gray",
                "dark_gray","blue","green","aqua","red","light_purple","yellow","white"};
        boolean valid = false;
        for (String value : allowed) if (value.equals(color)) { valid = true; break; }
        if (!valid) throw new IllegalArgumentException("Choose one of the standard Minecraft colors.");
        JsonObject p = new JsonObject(); p.addProperty("p_tag_color", color);
        rpc("set_clan_tag_color", p);
    }); }
    public void updateSettings(String name, String tag, String description, boolean isPrivate) { action("Clan settings updated.", () -> {
        validateClanIdentity(name, tag);
        JsonObject p = new JsonObject(); p.addProperty("p_clan_id", clanId()); p.addProperty("p_name", name.trim()); p.addProperty("p_tag", tag.trim().toUpperCase(java.util.Locale.ROOT));
        p.addProperty("p_description", description == null ? "" : description); p.addProperty("p_visibility", isPrivate ? "private" : "public"); rpc("update_clan_settings", p);
    }); }

    private static void validateClanIdentity(String name, String tag) {
        String cleanName = name == null ? "" : name.trim();
        String cleanTag = tag == null ? "" : tag.trim();
        if (cleanName.length() < 3 || cleanName.length() > 24)
            throw new IllegalArgumentException("Clan names must contain 3 to 24 characters.");
        if (!cleanTag.matches("[A-Za-z]{2,3}"))
            throw new IllegalArgumentException("Clan tags must contain 2 to 3 letters.");
    }

    private synchronized void heartbeatPresence() throws Exception {
        long now = System.currentTimeMillis();
        if (now - lastPresenceHeartbeat < 30000L) return;
        SupabaseCommunityService.clanRequestElement("POST",
                "/rest/v1/rpc/clan_presence_heartbeat", "{}");
        lastPresenceHeartbeat = now;
    }
    private synchronized void startPresenceHeartbeat() {
        if (presenceTask != null && !presenceTask.isCancelled() && !presenceTask.isDone()) return;
        presenceTask = presenceWorker.scheduleWithFixedDelay(() -> {
            if (!SupabaseCommunityService.isSignedIn()) return;
            try { heartbeatPresence(); }
            catch (Exception e) {
                System.err.println("[AlphaClient] Clan presence heartbeat failed: " + message(e));
            }
        }, 30L, 30L, TimeUnit.SECONDS);
    }
    private synchronized void stopPresenceHeartbeat() {
        if (presenceTask != null) {
            presenceTask.cancel(false);
            presenceTask = null;
        }
        lastPresenceHeartbeat = 0L;
    }
    private String clanId() {
        String id = str(snapshot.clan, "id"); if (id == null) throw new IllegalStateException("You are not in a clan."); return id;
    }
    private void rpcValue(String fn, String key, String value) throws Exception { JsonObject p = new JsonObject(); p.addProperty(key, value); rpc(fn, p); }
    private void rpc(String fn, JsonObject p) throws Exception {
        SupabaseCommunityService.clanRequestElement("POST", "/rest/v1/rpc/" + fn, p.toString());
    }
    private interface Op { void run() throws Exception; }
    private void action(String success, Op op) {
        if (!actionRunning.compareAndSet(false, true)) {
            snapshot.status = "Another clan action is still processing.";
            return;
        }
        worker.execute(() -> {
            snapshot.status = "Working...";
            try {
                op.run();
                Snapshot value = load();
                value.status = success;
                snapshot = value;
                lastRefresh = System.currentTimeMillis();
                String id = str(value.clan, "id");
                if (id == null) {
                    stopPresenceHeartbeat();
                    realtimeClan = null;
                    ClanRealtimeClient.INSTANCE.disconnect();
                } else {
                    startPresenceHeartbeat();
                    if (!id.equals(realtimeClan)) { realtimeClan = id; ClanRealtimeClient.INSTANCE.subscribe(id); }
                }
            } catch (Exception e) {
                Snapshot value = snapshot;
                value.status = message(e);
            } finally {
                actionRunning.set(false);
            }
        });
    }
    private static String enc(String value) throws Exception { return URLEncoder.encode(value, "UTF-8"); }
    private static String message(Exception e) {
        String raw = e.getMessage() == null ? "" : e.getMessage();
        String lower = raw.toLowerCase(java.util.Locale.ROOT);
        if (lower.contains("clans_name_normalized_uidx"))
            return "That clan name is already taken. Choose another name.";
        if (lower.contains("clans_tag_normalized_uidx"))
            return "That clan tag is already taken. Choose another tag.";
        if (lower.contains("duplicate key") || lower.contains("unique constraint"))
            return "That clan name or tag is already taken. Choose another one.";
        return raw.isEmpty() ? "Clan request failed." : raw;
    }
    public static String str(JsonObject o, String key) { return o == null || !o.has(key) || o.get(key).isJsonNull() ? null : o.get(key).getAsString(); }
    public static JsonObject nested(JsonObject o, String key) { return o != null && o.has(key) && o.get(key).isJsonObject() ? o.getAsJsonObject(key) : new JsonObject(); }
    public static String playerName(JsonObject row) {
        JsonObject p = nested(row, "profiles"); String name = str(p, "username");
        if (name == null) name = str(p, "display_name"); return name == null ? "Unknown player" : name;
    }
    public static final class Snapshot {
        public JsonArray browser = new JsonArray(), members = new JsonArray(), messages = new JsonArray(), invites = new JsonArray(), requests = new JsonArray();
        public JsonObject clan; public boolean owner, officer; public volatile String status = "Loading AlphaClans...";
    }
}
