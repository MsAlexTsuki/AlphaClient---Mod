package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Stores client or protocol version metadata. */
public class VersionAwareDetection {
    private final String version;
    private final Map<String, String> attributes = new HashMap<String, String>();
    public VersionAwareDetection(String version) {
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("version cannot be blank");
        }
        this.version = version.trim();
    }
    public String getVersion() { return version; }
    public void setAttribute(String name, String value) {
        if (name != null && !name.trim().isEmpty() && value != null) {
            attributes.put(name.trim(), value);
        }
    }
    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(new HashMap<String, String>(attributes));
    }
}