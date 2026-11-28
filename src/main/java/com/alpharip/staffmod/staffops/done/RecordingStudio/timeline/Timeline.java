package com.alpharip.staffmod.RecordingStudio.timeline;

import com.alpharip.staffmod.RecordingStudio.scene.RecordingStudioScene;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** General-purpose timeline and scene collection shared by Recording Studio modules. */
public final class Timeline {
    private final List<TimelineMarker> markers = new ArrayList<TimelineMarker>();
    private final List<TimelineTrack> tracks = new ArrayList<TimelineTrack>();
    private final List<RecordingStudioScene> scenes = new ArrayList<RecordingStudioScene>();
    private String activeSceneId;

    public Timeline() {
        this(true);
    }

    /** Allows persistence code to construct an empty model before restoring saved scenes. */
    public Timeline(boolean createDefaultScene) {
        if (createDefaultScene) {
            RecordingStudioScene main = RecordingStudioScene.createDefault("main", "Main Scene");
            scenes.add(main);
            activeSceneId = main.getId();
        }
    }

    public synchronized void addMarker(TimelineMarker marker) {
        if (marker == null) throw new IllegalArgumentException("Marker is required");
        markers.add(marker);
        Collections.sort(markers, new Comparator<TimelineMarker>() {
            public int compare(TimelineMarker a, TimelineMarker b) {
                return Long.compare(a.getTimeMillis(), b.getTimeMillis());
            }
        });
    }

    public synchronized void addTrack(TimelineTrack track) {
        if (track == null) throw new IllegalArgumentException("Track is required");
        for (TimelineTrack existing : tracks) {
            if (existing.getId().equals(track.getId())) {
                throw new IllegalArgumentException("Duplicate track id: " + track.getId());
            }
        }
        tracks.add(track);
    }

    public synchronized void addScene(RecordingStudioScene scene) {
        if (scene == null) throw new IllegalArgumentException("Scene is required");
        for (RecordingStudioScene existing : scenes) {
            if (existing.getId().equals(scene.getId())) {
                throw new IllegalArgumentException("Duplicate scene id: " + scene.getId());
            }
        }
        scenes.add(scene);
        if (activeSceneId == null) activeSceneId = scene.getId();
    }

    public synchronized boolean removeScene(String sceneId) {
        if (sceneId == null || scenes.size() <= 1) return false;
        for (int i = 0; i < scenes.size(); i++) {
            if (scenes.get(i).getId().equals(sceneId)) {
                scenes.remove(i);
                if (sceneId.equals(activeSceneId)) activeSceneId = scenes.get(0).getId();
                return true;
            }
        }
        return false;
    }

    public synchronized boolean setActiveScene(String sceneId) {
        if (sceneId == null) return false;
        for (RecordingStudioScene scene : scenes) {
            if (scene.getId().equals(sceneId)) {
                activeSceneId = sceneId;
                return true;
            }
        }
        return false;
    }

    public synchronized RecordingStudioScene getActiveScene() {
        for (RecordingStudioScene scene : scenes) {
            if (scene.getId().equals(activeSceneId)) return scene;
        }
        return scenes.isEmpty() ? null : scenes.get(0);
    }

    public synchronized String getActiveSceneId() {
        RecordingStudioScene active = getActiveScene();
        return active == null ? "" : active.getId();
    }

    public synchronized List<RecordingStudioScene> getScenes() {
        return Collections.unmodifiableList(new ArrayList<RecordingStudioScene>(scenes));
    }

    public synchronized List<TimelineMarker> getMarkers() {
        return Collections.unmodifiableList(new ArrayList<TimelineMarker>(markers));
    }

    public synchronized List<TimelineTrack> getTracks() {
        return Collections.unmodifiableList(new ArrayList<TimelineTrack>(tracks));
    }

    public synchronized long duration() {
        long duration = markers.isEmpty() ? 0L : markers.get(markers.size() - 1).getTimeMillis();
        for (TimelineTrack track : tracks) {
            for (TimelineClip clip : track.getClips()) duration = Math.max(duration, clip.getEnd());
        }
        return duration;
    }
}
