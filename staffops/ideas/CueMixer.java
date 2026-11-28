package com.alpharip.staffmod.staffops.ideas;
import java.util.LinkedHashMap;
import java.util.Map;
/** Per-category volume settings; sound interception and category mapping are pending. */
public final class CueMixer {
    private final Map<String, Float> volumes = new LinkedHashMap<String, Float>();
    public void setVolume(String category, float volume) {
        if (category != null && !category.trim().isEmpty()) volumes.put(category, Float.valueOf(clamp(volume)));
    }
    public float getVolume(String category, float fallback) {
        Float value = volumes.get(category); return value == null ? clamp(fallback) : value.floatValue();
    }
    private float clamp(float value) { return Math.max(0F, Math.min(1F, value)); }
    public Map<String, Float> getVolumes() { return new LinkedHashMap<String, Float>(volumes); }
}
