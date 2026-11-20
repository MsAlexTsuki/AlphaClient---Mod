package com.alpharip.staffmod.RecordingStudio.scene;

/** A configurable input used by a Recording Studio scene. */
public final class RecordingStudioSource {
    public enum Type {
        GAME_CAPTURE,
        MICROPHONE,
        GAME_AUDIO,
        DESKTOP,
        DISPLAY,
        WEBCAM
    }

    private final String id;
    private final String name;
    private final Type type;
    private boolean enabled;
    private String audioDeviceKey = "";
    private String screenDeviceId = "";
    private String webcamDeviceName = "";
    private int xPercent, yPercent, cropLeftPercent, cropTopPercent, cropRightPercent, cropBottomPercent;
    private int widthPercent = 100, heightPercent = 100;

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

    public synchronized String getWebcamDeviceName() { return webcamDeviceName; }
    public synchronized void setWebcamDeviceName(String value) {
        if (type != Type.WEBCAM) throw new IllegalStateException("Only Webcam sources select a camera");
        webcamDeviceName = value == null ? "" : value.trim();
    }
    public synchronized int getXPercent() { return xPercent; }
    public synchronized int getYPercent() { return yPercent; }
    public synchronized int getWidthPercent() { return widthPercent; }
    public synchronized int getHeightPercent() { return heightPercent; }
    public synchronized int getCropLeftPercent() { return cropLeftPercent; }
    public synchronized int getCropTopPercent() { return cropTopPercent; }
    public synchronized int getCropRightPercent() { return cropRightPercent; }
    public synchronized int getCropBottomPercent() { return cropBottomPercent; }
    public synchronized void setTransform(int x, int y, int width, int height) {
        xPercent = clamp(x, -100, 200); yPercent = clamp(y, -100, 200);
        widthPercent = clamp(width, 1, 300); heightPercent = clamp(height, 1, 300);
    }
    public synchronized void setCrop(int left, int top, int right, int bottom) {
        cropLeftPercent = clamp(left, 0, 95); cropTopPercent = clamp(top, 0, 95);
        cropRightPercent = clamp(right, 0, 95); cropBottomPercent = clamp(bottom, 0, 95);
        if (cropLeftPercent + cropRightPercent >= 100) cropRightPercent = 99 - cropLeftPercent;
        if (cropTopPercent + cropBottomPercent >= 100) cropBottomPercent = 99 - cropTopPercent;
    }
    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }

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
