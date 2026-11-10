package com.alpharip.staffmod.sounds.alertsound;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Local Alert Sounds prototype.
 *
 * Maps caller-defined event names to Minecraft sound identifiers. Other
 * modules can call trigger(eventName, player) when their event occurs.
 * This class deliberately does not subscribe to unspecified game events.
 */
public final class AlertSounds {
    private static final String ENABLED_KEY = "enabled";
    private static final String VOLUME_KEY = "volume";
    private static final String EVENT_PREFIX = "event.";

    private final File configFile;
    private final LinkedHashMap<String, String> soundMappings =
            new LinkedHashMap<String, String>();

    private boolean enabled = true;
    private float volume = 1.0F;

    public AlertSounds(File configDirectory) {
        if (configDirectory == null) {
            throw new IllegalArgumentException("Config directory is required.");
        }
        this.configFile = new File(configDirectory, "alpha-alert-sounds.properties");
        load();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        save();
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        if (Float.isNaN(volume) || Float.isInfinite(volume)) {
            throw new IllegalArgumentException("Volume must be a finite number.");
        }
        this.volume = Math.max(0.0F, Math.min(1.0F, volume));
        save();
    }

    public void setSound(String eventName, String soundId) {
        String event = requireValue(eventName, "Event name");
        String sound = requireValue(soundId, "Sound identifier");
        soundMappings.put(event, sound);
        save();
    }

    public String getSound(String eventName) {
        String event = requireValue(eventName, "Event name");
        return soundMappings.get(event);
    }

    public boolean removeSound(String eventName) {
        String event = requireValue(eventName, "Event name");
        boolean removed = soundMappings.remove(event) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public Map<String, String> getMappings() {
        return Collections.unmodifiableMap(
                new LinkedHashMap<String, String>(soundMappings));
    }

    /**
     * Plays the configured sound for an event. Call this from the feature
     * that owns the event; no event types are assumed by this prototype.
     */
    public boolean trigger(String eventName, EntityPlayer player) {
        if (!enabled || player == null || eventName == null) {
            return false;
        }

        String soundId = soundMappings.get(eventName.trim());
        if (soundId == null || soundId.trim().isEmpty()) {
            return false;
        }

        player.playSound(soundId, volume, 1.0F);
        return true;
    }

    public boolean testSound(String soundId, EntityPlayer player) {
        if (player == null || soundId == null || soundId.trim().isEmpty()) {
            return false;
        }
        player.playSound(soundId.trim(), volume, 1.0F);
        return true;
    }

    private void load() {
        if (!configFile.isFile()) {
            return;
        }

        Properties properties = new Properties();
        FileInputStream input = null;
        try {
            input = new FileInputStream(configFile);
            properties.load(input);

            enabled = Boolean.parseBoolean(properties.getProperty(ENABLED_KEY, "true"));
            try {
                setLoadedVolume(Float.parseFloat(properties.getProperty(VOLUME_KEY, "1.0")));
            } catch (NumberFormatException ignored) {
                volume = 1.0F;
            }

            soundMappings.clear();
            for (String key : properties.stringPropertyNames()) {
                if (key.startsWith(EVENT_PREFIX)) {
                    String eventName = key.substring(EVENT_PREFIX.length()).trim();
                    String soundId = properties.getProperty(key, "").trim();
                    if (!eventName.isEmpty() && !soundId.isEmpty()) {
                        soundMappings.put(eventName, soundId);
                    }
                }
            }
        } catch (IOException ignored) {
            // A missing or unreadable local config must not prevent the client from starting.
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ignored) {
                    // Nothing else can be done during config loading.
                }
            }
        }
    }

    private void setLoadedVolume(float value) {
        if (!Float.isNaN(value) && !Float.isInfinite(value)) {
            volume = Math.max(0.0F, Math.min(1.0F, value));
        }
    }

    private void save() {
        File parent = configFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            return;
        }

        Properties properties = new Properties();
        properties.setProperty(ENABLED_KEY, Boolean.toString(enabled));
        properties.setProperty(VOLUME_KEY, Float.toString(volume));
        for (Map.Entry<String, String> entry : soundMappings.entrySet()) {
            properties.setProperty(EVENT_PREFIX + entry.getKey(), entry.getValue());
        }

        FileOutputStream output = null;
        try {
            output = new FileOutputStream(configFile);
            properties.store(output, "AlphaClient Alert Sounds");
        } catch (IOException ignored) {
            // Keep the in-memory settings available if local persistence fails.
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ignored) {
                    // Nothing else can be done during config saving.
                }
            }
        }
    }

    private static String requireValue(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " must not be blank.");
        }
        return value.trim();
    }
}
