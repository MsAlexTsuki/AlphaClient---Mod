package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Assigns cosmetic stickers to saved bookmarks.
 * Category: Donator Scout
 * Foundation model only; this class is not wired into Minecraft or Forge.
 */
public class BookmarkStickers {
    public static final String FEATURE_NAME = "BookmarkStickers";
    public static final String CATEGORY = "Donator Scout";
    public static final String DESCRIPTION = "Assigns cosmetic stickers to saved bookmarks.";

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
