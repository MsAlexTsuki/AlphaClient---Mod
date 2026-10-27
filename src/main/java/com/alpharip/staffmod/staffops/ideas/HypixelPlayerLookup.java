package com.alpharip.staffmod.staffops.ideas;

/**
 * Foundation for a Hypixel player lookup panel.
 * Network/API requests and GUI integration are intentionally not connected yet.
 */
public final class HypixelPlayerLookup {
    public static final String FEATURE_NAME = "Hypixel Player Lookup";

    private boolean enabled;
    private String playerName = "";
    private String uuid = "";
    private String status = "Not searched";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getPlayerName() { return playerName; }
    public String getUuid() { return uuid; }
    public String getStatus() { return status; }

    /** Starts a new lookup state after validating a Minecraft-style username. */
    public boolean beginLookup(String name) {
        if (name == null) return fail("Enter a player name");
        String candidate = name.trim();
        if (!candidate.matches("[A-Za-z0-9_]{3,16}")) return fail("Invalid player name");
        playerName = candidate;
        uuid = "";
        status = "Waiting for API result";
        return true;
    }

    /** Stores a result supplied by a future API adapter; UUID format is validated. */
    public boolean applyResult(String resolvedName, String resolvedUuid) {
        if (resolvedName == null || resolvedUuid == null
                || !resolvedUuid.replace("-", "").matches("(?i)[0-9a-f]{32}")) {
            return fail("Invalid lookup result");
        }
        playerName = resolvedName.trim();
        uuid = resolvedUuid.trim();
        status = "Result available";
        return true;
    }

    public void setUnavailable(String reason) {
        status = reason == null || reason.trim().isEmpty() ? "Lookup unavailable" : reason.trim();
    }

    private boolean fail(String message) {
        status = message;
        return false;
    }
}
