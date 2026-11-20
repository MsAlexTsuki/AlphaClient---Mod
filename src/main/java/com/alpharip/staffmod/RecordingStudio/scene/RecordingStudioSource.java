package com.alpharip.staffmod.RecordingStudio.scene;

/** A configurable input used by a Recording Studio scene. */
public final class RecordingStudioSource {
    public enum Type {
        GAME_CAPTURE,
        MICROPHONE,
        GAME_AUDIO,
        DESKTOP,
        DISPLAY
    }

    private final String id;
    private final String name;
    private final Type type;
    private boolean enabled;
    private String audioDeviceKey = "";
    private String screenDeviceId = "";

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

    /** Optional per-scene input-device override; an empty key uses the project fallback. */
    public synchronized String getAudioDeviceKey() { return audioDeviceKey; }
    public synchronized void setAudioDeviceKey(String value) {
        if (type != Type.MICROPHONE && type != Type.GAME_AUDIO) throw new IllegalStateException("This source does not use an audio input device");
        audioDeviceKey = value == null ? "" : value.trim();
    }

    /** Stable AWT device identifier used only by an additional Display source. */
    public synchronized String getScreenDeviceId() { return screenDeviceId; }
    public synchronized void setScreenDeviceId(String value) {
        if (type != Type.DISPLAY) throw new IllegalStateException("Only Display sources select a monitor");
        screenDeviceId = value == null ? "" : value.trim();
    }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
