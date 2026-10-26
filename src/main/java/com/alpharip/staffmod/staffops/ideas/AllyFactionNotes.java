package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ally Faction Notes.
 * Purpose: Stores notes for allied factions.
 * Required access: Everyone.
 * Foundation only: Minecraft/Forge event, GUI, rendering, and external-service integration are separate work.
 */
public final class AllyFactionNotes {
    public static final String NAME = "Ally Faction Notes";
    public static final String CATEGORY = "HardcoreFactions";
    public static final String STATUS = "NEW";
    public static final String PURPOSE = "Stores notes for allied factions.";
    public static final String REQUIRED_ACCESS = "Everyone";
    public static final boolean REQUIRES_SUPABASE = false;
    public static final boolean REQUIRES_SERVER_SUPPORT = false;

    public static final class Entry {
        private final String id;
        private final Map<String, String> fields;
        private final long createdAt;
        private long updatedAt;
        private Entry(String id, Map<String, String> fields) {
            this.id = id;
            this.fields = Collections.unmodifiableMap(new LinkedHashMap<String, String>(fields));
            this.createdAt = System.currentTimeMillis();
            this.updatedAt = this.createdAt;
        }
        public String getId() { return id; }
        public Map<String, String> getFields() { return fields; }
        public long getCreatedAt() { return createdAt; }
        public long getUpdatedAt() { return updatedAt; }
    }

    private final Map<String, Entry> entries = new LinkedHashMap<String, Entry>();

    public synchronized Entry put(String id, Map<String, String> fields) {
        String key = requireText(id, "id");
        if (fields == null) throw new IllegalArgumentException("fields must not be null.");
        Entry entry = new Entry(key, fields);
        entries.put(key, entry);
        return entry;
    }

    public synchronized Entry get(String id) { return entries.get(requireText(id, "id")); }
    public synchronized boolean remove(String id) { return entries.remove(requireText(id, "id")) != null; }
    public synchronized List<Entry> snapshot() { return Collections.unmodifiableList(new ArrayList<Entry>(entries.values())); }
    public synchronized int size() { return entries.size(); }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(field + " must not be blank.");
        return value.trim();
    }
}
