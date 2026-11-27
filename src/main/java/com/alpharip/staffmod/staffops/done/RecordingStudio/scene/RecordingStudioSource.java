package com.alpharip.staffmod.RecordingStudio.scene;

/** A configurable input used by a Recording Studio scene. */
public final class RecordingStudioSource {
    public enum Type {
        GAME_CAPTURE,
        MICROPHONE
    }

    private final String id;
    private final String name;
    private final Type type;
    private boolean enabled;

    public RecordingStudioSource(String id, String name, Type type, boolean enabled) {
        if (blank(id) || blank(name) || type == null) {
            throw new IllegalArgumentException("Source ID, name and type are required");
        }
        this.id = id.trim();
        this.name = name.trim();
        this.type = type;
        this.enabled = enabled;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Type getType() { return type; }
    public synchronized boolean isEnabled() { return enabled; }
    public synchronized void setEnabled(boolean value) { enabled = value; }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
