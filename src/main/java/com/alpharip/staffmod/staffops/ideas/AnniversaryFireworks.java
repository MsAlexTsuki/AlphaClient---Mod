package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Checks configured anniversary dates and records eligible celebrations.
 * Category: Founder Fun
 * Foundation model only; this class is not wired into Minecraft or Forge.
 */
public class AnniversaryFireworks {
    public static final String FEATURE_NAME = "AnniversaryFireworks";
    public static final String CATEGORY = "Founder Fun";
    public static final String DESCRIPTION = "Checks configured anniversary dates and records eligible celebrations.";

    private boolean enabled = true;
    private final List<String> entries = new ArrayList<String>();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void addEntry(String entry) {
        if (entry != null && !entry.trim().isEmpty()) entries.add(entry.trim());
    }
    public boolean removeEntry(String entry) { return entries.remove(entry); }
    public List<String> getEntries() { return Collections.unmodifiableList(new ArrayList<String>(entries)); }
    public void clearEntries() { entries.clear(); }
}
