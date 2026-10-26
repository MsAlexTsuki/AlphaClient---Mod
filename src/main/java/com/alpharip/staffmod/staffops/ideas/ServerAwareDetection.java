package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Stores server identifiers and context metadata. */
public class ServerAwareDetection {
    private final String serverId;
    private final Map<String, String> attributes = new HashMap<String, String>();
    public ServerAwareDetection(String serverId) {
        if (serverId == null || serverId.trim().isEmpty()) {
            throw new IllegalArgumentException("serverId cannot be blank");
        }
        this.serverId = serverId.trim();
    }
    public String getServerId() { return serverId; }
    public void setAttribute(String name, String value) {
        if (name != null && !name.trim().isEmpty() && value != null) {
            attributes.put(name.trim(), value);
        }
    }
    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(new HashMap<String, String>(attributes));
    }
}