package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Foundation for tracking playtime grouped by server.
 * TODO(Supabase): persist cumulative playtime per user and server.
 * Session lifecycle hooks, synchronization, UI, and tests remain to be implemented.
 */
public class PlaytimePerServer {
    private final Map<String, Long> playtimeMillisByServer = new LinkedHashMap<String, Long>();

    public void addPlaytime(String serverId, long durationMillis) {
        if (serverId == null || serverId.trim().isEmpty() || durationMillis <= 0L) return;
        Long current = playtimeMillisByServer.get(serverId);
        playtimeMillisByServer.put(serverId, (current == null ? 0L : current) + durationMillis);
    }

    public long getPlaytimeMillis(String serverId) {
        Long value = playtimeMillisByServer.get(serverId);
        return value == null ? 0L : value;
    }

    public Map<String, Long> getPlaytimeByServer() {
        return Collections.unmodifiableMap(new LinkedHashMap<String, Long>(playtimeMillisByServer));
    }
}