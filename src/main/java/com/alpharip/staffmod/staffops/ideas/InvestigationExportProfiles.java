package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Local named export profiles describing which investigation sections to include. */
public final class InvestigationExportProfiles {
    public static final class Profile {
        private final String name;
        private final List<String> sections;
        private final String format;

        public Profile(String name, List<String> sections, String format) {
            this.name = name == null ? "" : name.trim();
            this.sections = sections == null ? new ArrayList<String>() : new ArrayList<String>(sections);
            this.format = format == null ? "text" : format.trim();
        }
        public String getName() { return name; }
        public List<String> getSections() {
            return Collections.unmodifiableList(new ArrayList<String>(sections));
        }
        public String getFormat() { return format; }
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