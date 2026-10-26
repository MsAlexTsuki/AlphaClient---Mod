package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Saves named local workspace preferences such as selected tabs and panel order. */
public final class InvestigationWorkspaceProfiles {
    public static final class Profile {
        private final String name;
        private final Map<String, String> settings;

        public Profile(String name, Map<String, String> settings) {
            this.name = name == null ? "" : name.trim();
            this.settings = settings == null ? new LinkedHashMap<String, String>()
                : new LinkedHashMap<String, String>(settings);
        }
        public String getName() { return name; }
        public Map<String, String> getSettings() {
            return Collections.unmodifiableMap(new LinkedHashMap<String, String>(settings));
        }
    }

    private final Map<String, Profile> profiles = new LinkedHashMap<String, Profile>();
    public synchronized void save(Profile profile) {
        if (profile != null && !profile.getName().isEmpty()) profiles.put(profile.getName(), profile);
    }
    public synchronized Profile get(String name) { return profiles.get(name); }
    public synchronized boolean remove(String name) { return profiles.remove(name) != null; }
    public synchronized List<String> getNames() {
        return Collections.unmodifiableList(new ArrayList<String>(profiles.keySet()));
    }
}