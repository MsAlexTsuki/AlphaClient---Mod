package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Tracks event preparation checklist items. Category: Cross-Category. Foundation model only; no Minecraft/Forge integration. */
public class EventPrepAssistant {
    public static final String FEATURE_NAME = "EventPrepAssistant";
    public static final String CATEGORY = "Cross-Category";
    public static final String DESCRIPTION = "Tracks event preparation checklist items.";
    private boolean enabled = true;
    private final List<String> entries = new ArrayList<String>();
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void addEntry(String entry) { if (entry != null && !entry.trim().isEmpty()) entries.add(entry.trim()); }
    public boolean removeEntry(String entry) { return entries.remove(entry); }
    public List<String> getEntries() { return Collections.unmodifiableList(new ArrayList<String>(entries)); }
    public void clearEntries() { entries.clear(); }
}
