package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Stores baseline measurements associated with a player identifier. */
public class PlayerSpecificBaseline {
    private final String playerId;
    private final Map<String, String> attributes = new HashMap<String, String>();
    public PlayerSpecificBaseline(String playerId) {
        if (playerId == null || playerId.trim().isEmpty()) {
            throw new IllegalArgumentException("playerId cannot be blank");
        }
        this.playerId = playerId.trim();
    }
    public String getPlayerId() { return playerId; }
    public void setAttribute(String name, String value) {
        if (name != null && !name.trim().isEmpty() && value != null) {
            attributes.put(name.trim(), value);
        }
    }
    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(new HashMap<String, String>(attributes));
    }
}