package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Stores named detection settings as simple key/value profiles. */
public class DetectionProfiles {
    private final Map<String, Map<String, String>> profiles =
            new HashMap<String, Map<String, String>>();

    public boolean create(String name) {
        String key = normalize(name);
        if (key == null || profiles.containsKey(key)) {
            return false;
        }
        profiles.put(key, new HashMap<String, String>());
        return true;
    }

    public boolean setValue(String profileName, String setting, String value) {
        String profile = normalize(profileName);
        String settingKey = normalize(setting);
        if (profile == null || settingKey == null || value == null
                || !profiles.containsKey(profile)) {
            return false;
        }
        profiles.get(profile).put(settingKey, value);
        return true;
    }

    public String getValue(String profileName, String setting) {
        String profile = normalize(profileName);
        String settingKey = normalize(setting);
        if (profile == null || settingKey == null || !profiles.containsKey(profile)) {
            return null;
        }
        return profiles.get(profile).get(settingKey);
    }

    public boolean remove(String name) {
        String key = normalize(name);
        return key != null && profiles.remove(key) != null;
    }

    public Map<String, Map<String, String>> snapshot() {
        Map<String, Map<String, String>> copy =
                new HashMap<String, Map<String, String>>();
        for (Map.Entry<String, Map<String, String>> entry : profiles.entrySet()) {
            copy.put(entry.getKey(), Collections.unmodifiableMap(
                    new HashMap<String, String>(entry.getValue())));
        }
        return Collections.unmodifiableMap(copy);
    }

    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
