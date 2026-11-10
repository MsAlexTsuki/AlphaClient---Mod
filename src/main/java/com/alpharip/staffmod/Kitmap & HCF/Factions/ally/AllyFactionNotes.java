package com.alpharip.staffmod.kitmaphcf.factions.ally;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Local notes grouped by allied faction name.
 *
 * This class is deliberately independent from AlphaClient core and Supabase.
 */
public final class AllyFactionNotes {
    public static final String NAME = "Ally Faction Notes";
    public static final String CATEGORY = "HardcoreFactions";
    public static final String STATUS = "PROTOTYPE";
    public static final String REQUIRED_ACCESS = "Unconfirmed";

    private final Map<String, List<String>> notes =
            new LinkedHashMap<String, List<String>>();

    public synchronized void add(String faction, String note) {
        String factionName = requireText(faction, "Faction name");
        String noteText = requireText(note, "Note");
        String key = findFactionKey(factionName);
        if (key == null) {
            key = factionName;
            notes.put(key, new ArrayList<String>());
        }
        notes.get(key).add(noteText);
    }

    public synchronized List<String> get(String faction) {
        String key = findFactionKey(requireText(faction, "Faction name"));
        if (key == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<String>(notes.get(key)));
    }

    public synchronized List<String> getFactionNames() {
        return Collections.unmodifiableList(new ArrayList<String>(notes.keySet()));
    }

    public synchronized void clear(String faction) {
        String key = findFactionKey(requireText(faction, "Faction name"));
        if (key != null) {
            notes.remove(key);
        }
    }

    public synchronized Map<String, List<String>> snapshot() {
        Map<String, List<String>> copy = new LinkedHashMap<String, List<String>>();
        for (Map.Entry<String, List<String>> entry : notes.entrySet()) {
            copy.put(entry.getKey(),
                    Collections.unmodifiableList(new ArrayList<String>(entry.getValue())));
        }
        return Collections.unmodifiableMap(copy);
    }

    synchronized void replaceAll(Map<String, List<String>> saved) {
        notes.clear();
        if (saved == null) {
            return;
        }
        for (Map.Entry<String, List<String>> entry : saved.entrySet()) {
            if (entry.getKey() == null || entry.getKey().trim().isEmpty()
                    || entry.getValue() == null) {
                continue;
            }
            String faction = entry.getKey().trim();
            List<String> cleanNotes = new ArrayList<String>();
            for (String note : entry.getValue()) {
                if (note != null && !note.trim().isEmpty()) {
                    cleanNotes.add(note.trim());
                }
            }
            if (!cleanNotes.isEmpty()) {
                notes.put(faction, cleanNotes);
            }
        }
    }

    private String findFactionKey(String faction) {
        for (String existing : notes.keySet()) {
            if (existing.equalsIgnoreCase(faction)) {
                return existing;
            }
        }
        return null;
    }

    private static String requireText(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be empty.");
        }
        return value.trim();
    }
}
