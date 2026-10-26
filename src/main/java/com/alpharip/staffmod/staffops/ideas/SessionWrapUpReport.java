package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Builds a session summary from supplied session details. Category: Cross-Category. Foundation model only; no Minecraft/Forge integration. */
public class SessionWrapUpReport {
    public static final String FEATURE_NAME = "SessionWrapUpReport";
    public static final String CATEGORY = "Cross-Category";
    public static final String DESCRIPTION = "Builds a session summary from supplied session details.";
    private boolean enabled = true;
    private final List<String> entries = new ArrayList<String>();
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void addEntry(String entry) { if (entry != null && !entry.trim().isEmpty()) entries.add(entry.trim()); }
    public boolean removeEntry(String entry) { return entries.remove(entry); }
    public List<String> getEntries() { return Collections.unmodifiableList(new ArrayList<String>(entries)); }
    public void clearEntries() { entries.clear(); }
}
