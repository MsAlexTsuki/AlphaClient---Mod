package com.alpharip.staffmod.rankviewer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
 * Background cache for the public rank viewer. The render thread only reads this cache;
 * Supabase requests are batched and never performed during rendering.
 */
public final class RankCache {
    public static final RankCache INSTANCE = new RankCache();

    private static final String SUPABASE_URL = "https://lescvpixttmmbnaluidd.supabase.co";
    private static final String PUBLISHABLE_KEY = "sb_publishable_DEeIl6IL4mPXiEx6SaJ3EQ_OvNhEPP8";
    private static final long TTL_MS = 5L * 60L * 1000L;
    private static final long RETRY_MS = 30L * 1000L;
    private static final int BATCH_SIZE = 100;

    private static final class Entry {
        final AlphaRank rank; // null means no linked Alpha community account
        final long expiresAt;
        Entry(AlphaRank rank, long expiresAt) {
            this.rank = rank;
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

    private RankCache() { }

    public synchronized void start() {
        if (executor != null && !executor.isShutdown()) return;
        executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "AlphaClient-RankFetcher");
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

    /** Returns the cached rank immediately and schedules a refresh when necessary. */
    public AlphaRank get(UUID uuid) {
        if (uuid == null) return null;
        long now = System.currentTimeMillis();
        Entry entry = cache.get(uuid);
        if (entry == null || entry.expiresAt <= now) {
            Long retry = retryAfter.get(uuid);
            if ((retry == null || retry <= now) && !inFlight.contains(uuid)) {
                pending.add(uuid);
            }
        }
        return entry == null ? null : entry.rank;
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
            Map<UUID, AlphaRank> found = fetch(batch);
            long expiresAt = now + TTL_MS;
            for (UUID uuid : batch) {
                cache.put(uuid, new Entry(found.get(uuid), expiresAt));
                retryAfter.remove(uuid);
            }
        } catch (Exception exception) {
            long retryAt = now + RETRY_MS;
            for (UUID uuid : batch) retryAfter.put(uuid, retryAt);
        } finally {
            inFlight.removeAll(batch);
        }
    }

    private Map<UUID, AlphaRank> fetch(List<UUID> uuids) throws IOException {
        JsonArray input = new JsonArray();
        for (UUID uuid : uuids) input.add(uuid.toString());
        JsonObject body = new JsonObject();
        body.add("uuids", input);

        HttpURLConnection connection = (HttpURLConnection) new URL(
                SUPABASE_URL + "/rest/v1/rpc/get_public_ranks"
        ).openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("apikey", PUBLISHABLE_KEY);
        connection.setRequestProperty("Authorization", "Bearer " + PUBLISHABLE_KEY);

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
                throw new IOException("Rank lookup returned HTTP " + status);
            }

            InputStreamReader reader = new InputStreamReader(
                    connection.getInputStream(), StandardCharsets.UTF_8);
            try {
                JsonElement parsed = new JsonParser().parse(reader);
                if (!parsed.isJsonArray()) throw new IOException("Rank lookup returned an invalid payload.");
                JsonArray rows = parsed.getAsJsonArray();
                Map<UUID, AlphaRank> result = new HashMap<UUID, AlphaRank>();
                for (JsonElement element : rows) {
                    if (!element.isJsonObject()) continue;
                    JsonObject row = element.getAsJsonObject();
                    if (!row.has("mc_uuid") || row.get("mc_uuid").isJsonNull()
                            || !row.has("rank") || row.get("rank").isJsonNull()) continue;
                    try {
                        result.put(UUID.fromString(row.get("mc_uuid").getAsString()),
                                AlphaRank.fromString(row.get("rank").getAsString()));
                    } catch (IllegalArgumentException ignored) {
                        // Ignore malformed rows instead of failing the whole batch.
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
