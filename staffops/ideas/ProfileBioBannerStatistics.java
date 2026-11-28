package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Profile Bio, Banner and Statistics. Provides a client-side data foundation for profile bio, banner and statistics; integration and user interface work remain separate.
 * Foundation only; service integration, Minecraft hooks and UI are separate work.
 */
public final class ProfileBioBannerStatistics {
    public static final String FEATURE_NAME = "Profile Bio, Banner and Statistics";
    public static final String DESCRIPTION = "Provides a client-side data foundation for profile bio, banner and statistics; integration and user interface work remain separate.";
    private boolean enabled = true;
    private final Map<String, String> settings = new LinkedHashMap<String, String>();
    private final List<String> entries = new ArrayList<String>();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public void setSetting(String key, String value) {
        if (key == null || key.trim().isEmpty()) return;
        if (value == null) settings.remove(key); else settings.put(key, value);
    }
    public String getSetting(String key, String fallback) {
        String value = settings.get(key);
        return value == null ? fallback : value;
    }
    public Map<String, String> getSettings() {
        return Collections.unmodifiableMap(new LinkedHashMap<String, String>(settings));
    }
    public void addEntry(String value) { if (value != null && !value.trim().isEmpty()) entries.add(value); }
    public List<String> getEntries() { return Collections.unmodifiableList(new ArrayList<String>(entries)); }
    public List<String> searchEntries(String query) {
        List<String> result = new ArrayList<String>();
        if (query == null || query.trim().isEmpty()) return getEntries();
        String normalized = query.toLowerCase(Locale.ROOT);
        for (String entry : entries) if (entry.toLowerCase(Locale.ROOT).contains(normalized)) result.add(entry);
        return Collections.unmodifiableList(result);
    }
    public void clearEntries() { entries.clear(); }
}
