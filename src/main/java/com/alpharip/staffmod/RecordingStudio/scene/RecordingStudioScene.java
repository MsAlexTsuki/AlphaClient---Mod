package com.alpharip.staffmod.RecordingStudio.scene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A named Recording Studio scene containing independently enabled capture sources. */
public final class RecordingStudioScene {
    private final String id;
    private String name;
    private final List<RecordingStudioSource> sources = new ArrayList<RecordingStudioSource>();

    public RecordingStudioScene(String id, String name) {
        if (blank(id) || blank(name)) throw new IllegalArgumentException("Scene ID and name are required");
        this.id = id.trim();
        this.name = name.trim();
    }

    public static RecordingStudioScene createDefault(String id, String name) {
        RecordingStudioScene scene = new RecordingStudioScene(id, name);
        scene.addSource(new RecordingStudioSource("game-capture", "Minecraft Capture",
                RecordingStudioSource.Type.GAME_CAPTURE, true));
        scene.addSource(new RecordingStudioSource("microphone", "Microphone",
                RecordingStudioSource.Type.MICROPHONE, true));
        return scene;
    }

    public String getId() { return id; }
    public synchronized String getName() { return name; }

    public synchronized void setName(String value) {
        if (blank(value)) throw new IllegalArgumentException("Scene name is required");
        name = value.trim();
    }

    public synchronized void addSource(RecordingStudioSource source) {
        if (source == null) throw new IllegalArgumentException("Source is required");
        if (findSource(source.getId()) != null) {
            throw new IllegalArgumentException("Duplicate source ID: " + source.getId());
        }
        sources.add(source);
    }

    public synchronized boolean removeSource(String sourceId) {
        for (int i = 0; i < sources.size(); i++) {
            if (sources.get(i).getId().equals(sourceId)) {
                sources.remove(i);
                return true;
            }
        }
        return false;
    }

    /** Moves a source one position without changing its identity or enabled state. */
    public synchronized boolean moveSource(String sourceId, int direction) {
        if (sourceId == null || (direction != -1 && direction != 1)) return false;
        for (int i = 0; i < sources.size(); i++) {
            if (!sources.get(i).getId().equals(sourceId)) continue;
            int target = i + direction;
            if (target < 0 || target >= sources.size()) return false;
            Collections.swap(sources, i, target);
            return true;
        }
        return false;
    }

    public synchronized RecordingStudioSource findSource(String sourceId) {
        if (sourceId == null) return null;
        for (RecordingStudioSource source : sources) {
            if (source.getId().equals(sourceId)) return source;
        }
        return null;
    }

    public synchronized RecordingStudioSource findSource(RecordingStudioSource.Type type) {
        if (type == null) return null;
        for (RecordingStudioSource source : sources) {
            if (source.getType() == type) return source;
        }
        return null;
    }

    public synchronized List<RecordingStudioSource> getSources() {
        return Collections.unmodifiableList(new ArrayList<RecordingStudioSource>(sources));
    }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
