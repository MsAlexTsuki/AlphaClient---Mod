// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.rankviewer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Background cache for public clan tags. The render thread only reads this cache;
 * Supabase requests are batched and never performed while drawing nametags.
 */
public final class ClanTagCache {
    public static final ClanTagCache INSTANCE = new ClanTagCache();

    private static final String SUPABASE_URL = "https://lescvpixttmmbnaluidd.supabase.co";
    private static final String PUBLISHABLE_KEY = "sb_publishable_DEeIl6IL4mPXiEx6SaJ3EQ_OvNhEPP8";
    private static final long TTL_MS = 30L * 1000L;
    private static final long MISS_TTL_MS = 10L * 1000L;
    private static final long RETRY_MS = 15L * 1000L;
    private static final int BATCH_SIZE = 100;

    private static final class Entry {
        final String tag;
        final long expiresAt;
        Entry(String tag, long expiresAt) {
            this.tag = tag;
            this.expiresAt = expiresAt;
        }
    }

    private final ConcurrentMap<UUID, Entry> cache = new ConcurrentHashMap<UUID, Entry>();
    private final ConcurrentMap<UUID, Long> retryAfter = new ConcurrentHashMap<UUID, Long>();
    private final java.util.Set<UUID> pending =
            java.util.Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());
    private final java.util.Set<UUID> inFlight =
            java.util.Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());
    private ScheduledExecutorService executor;

    private ClanTagCache() { }

    public synchronized void start() {
        if (executor != null && !executor.isShutdown()) return;
        executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "AlphaClient-ClanTagFetcher");
                thread.setDaemon(true);
                return thread;
            }
        });
        executor.scheduleWithFixedDelay(new Runnable() {
            @Override public void run() { flush(); }
        }, 1L, 1L, TimeUnit.SECONDS);
    }

    public synchronized void stop() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        pending.clear();
        inFlight.clear();
    }

    /** Returns the cached public clan tag immediately and queues stale UUIDs for refresh. */
    public String get(UUID uuid) {
        if (uuid == null) return null;
        long now = System.currentTimeMillis();
        Entry entry = cache.get(uuid);
        if (entry == null || entry.expiresAt <= now) {
            Long retry = retryAfter.get(uuid);
            if ((retry == null || retry <= now) && !inFlight.contains(uuid)) pending.add(uuid);
        }
        return entry == null || entry.expiresAt <= now ? null : entry.tag;
    }

    private void flush() {
        List<UUID> batch = new ArrayList<UUID>(BATCH_SIZE);
        Iterator<UUID> iterator = pending.iterator();
        while (iterator.hasNext() && batch.size() < BATCH_SIZE) {
            UUID uuid = iterator.next();
            if (inFlight.add(uuid)) {
                batch.add(uuid);
                pending.remove(uuid);
            }
        }
        if (batch.isEmpty()) return;

        long now = System.currentTimeMillis();
        try {
            Map<UUID, String> found = fetch(batch);
            for (UUID uuid : batch) {
                String tag = found.get(uuid);
                cache.put(uuid, new Entry(tag, now + (tag == null ? MISS_TTL_MS : TTL_MS)));
                retryAfter.remove(uuid);
            }
        } catch (Exception exception) {
            long retryAt = now + RETRY_MS;
            for (UUID uuid : batch) retryAfter.put(uuid, retryAt);
            System.err.println("[AlphaClient] Public clan-tag lookup failed; will retry: " + exception.getMessage());
        } finally {
            inFlight.removeAll(batch);
        }
    }

    private Map<UUID, String> fetch(List<UUID> uuids) throws IOException {
        JsonArray input = new JsonArray();
        for (UUID uuid : uuids) input.add(new JsonPrimitive(uuid.toString()));
        JsonObject body = new JsonObject();
        body.add("uuids", input);

        HttpURLConnection connection = (HttpURLConnection) new URL(
                SUPABASE_URL + "/rest/v1/rpc/get_public_clan_tags"
        ).openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("apikey", PUBLISHABLE_KEY);

        try {
            byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
            connection.setRequestProperty("Content-Length", String.valueOf(bytes.length));
            OutputStream output = connection.getOutputStream();
            try {
                output.write(bytes);
            } finally {
                output.close();
            }

            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                InputStream error = connection.getErrorStream();
                if (error != null) error.close();
                throw new IOException("Clan-tag lookup returned HTTP " + status);
            }

            InputStreamReader reader = new InputStreamReader(
                    connection.getInputStream(), StandardCharsets.UTF_8);
            try {
                JsonElement parsed = new JsonParser().parse(reader);
                if (!parsed.isJsonArray()) throw new IOException("Clan-tag lookup returned an invalid payload.");
                Map<UUID, String> result = new HashMap<UUID, String>();
                for (JsonElement element : parsed.getAsJsonArray()) {
                    if (!element.isJsonObject()) continue;
                    JsonObject row = element.getAsJsonObject();
                    if (!row.has("mc_uuid") || row.get("mc_uuid").isJsonNull()
                            || !row.has("clan_tag") || row.get("clan_tag").isJsonNull()) continue;
                    try {
                        String tag = row.get("clan_tag").getAsString();
                        if (tag.matches("[A-Z0-9]{2,5}")) {
                            result.put(UUID.fromString(row.get("mc_uuid").getAsString()), tag);
                        }
                    } catch (IllegalArgumentException ignored) {
                        // Ignore malformed rows instead of failing the entire lookup batch.
                    }
                }
                return result;
            } finally {
                reader.close();
            }
        } finally {
            connection.disconnect();
        }
    }
}
