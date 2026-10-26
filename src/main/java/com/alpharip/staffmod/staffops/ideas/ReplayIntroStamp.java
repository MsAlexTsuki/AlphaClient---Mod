package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Builds a replay intro label from a player name and title. Category: Donator Elite. Foundation model only; no Minecraft/Forge integration. */
public class ReplayIntroStamp {
    public static final String FEATURE_NAME = "ReplayIntroStamp";
    public static final String CATEGORY = "Donator Elite";
    public static final String DESCRIPTION = "Builds a replay intro label from a player name and title.";
    private boolean enabled = true;
    private final List<String> entries = new ArrayList<String>();
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void addEntry(String entry) { if (entry != null && !entry.trim().isEmpty()) entries.add(entry.trim()); }
    public boolean removeEntry(String entry) { return entries.remove(entry); }
    public List<String> getEntries() { return Collections.unmodifiableList(new ArrayList<String>(entries)); }
    public void clearEntries() { entries.clear(); }
}
