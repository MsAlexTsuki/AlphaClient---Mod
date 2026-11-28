package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Foundation for global playtime and level leaderboard entries.
 * TODO(Supabase): query leaderboard data and enforce privacy/opt-in settings.
 * Ranking updates, pagination, UI, and tests remain to be implemented.
 */
public class GlobalPlaytimeAndLevelLeaderboards {
    public static class Entry {
        private final String playerId;
        private final long playtimeMillis;
        private final int level;

        public Entry(String playerId, long playtimeMillis, int level) {
            this.playerId = playerId;
            this.playtimeMillis = Math.max(0L, playtimeMillis);
            this.level = Math.max(0, level);
        }
        public String getPlayerId() { return playerId; }
        public long getPlaytimeMillis() { return playtimeMillis; }
        public int getLevel() { return level; }
    }

    private final List<Entry> entries = new ArrayList<Entry>();

    public void addEntry(Entry entry) { if (entry != null) entries.add(entry); }
    public List<Entry> getByPlaytimeDescending() {
        List<Entry> result = new ArrayList<Entry>(entries);
        Collections.sort(result, new Comparator<Entry>() {
            @Override public int compare(Entry a, Entry b) {
                return Long.compare(b.getPlaytimeMillis(), a.getPlaytimeMillis());
            }
        });
        return Collections.unmodifiableList(result);
    }
    public List<Entry> getByLevelDescending() {
        List<Entry> result = new ArrayList<Entry>(entries);
        Collections.sort(result, new Comparator<Entry>() {
            @Override public int compare(Entry a, Entry b) {
                return Integer.compare(b.getLevel(), a.getLevel());
            }
        });
        return Collections.unmodifiableList(result);
    }
}