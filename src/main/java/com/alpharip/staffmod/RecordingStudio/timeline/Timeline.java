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
    private final List<TimelineTransition> transitions = new ArrayList<TimelineTransition>();
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

    public synchronized TimelineMarker moveMarker(TimelineMarker marker, long newTimeMillis) {
        if (marker == null || newTimeMillis < 0 || !markers.contains(marker)) return null;
        TimelineMarker moved = new TimelineMarker(newTimeMillis, marker.getLabel(), marker.getKind());
        markers.remove(marker);
        markers.add(moved);
        Collections.sort(markers, new Comparator<TimelineMarker>() {
            public int compare(TimelineMarker a, TimelineMarker b) {
                return Long.compare(a.getTimeMillis(), b.getTimeMillis());
            }
        });
        return moved;
    }

    public synchronized boolean removeMarker(TimelineMarker marker) {
        return marker != null && markers.remove(marker);
    }

    public synchronized List<TimelineTrack> getTracks() {
        return Collections.unmodifiableList(new ArrayList<TimelineTrack>(tracks));
    }

    /** Returns only transitions that currently connect adjacent imported video clips. */
    public synchronized List<TimelineTransition> getTransitions() {
        List<TimelineTransition> valid = new ArrayList<TimelineTransition>();
        for (TimelineTransition transition : transitions) {
            if (isValidTransition(transition)) valid.add(transition);
        }
        return Collections.unmodifiableList(valid);
    }

    /** Returns persisted transitions whose two referenced video clips still exist, even while temporarily non-adjacent. */
    public synchronized List<TimelineTransition> getStoredTransitions() {
        List<TimelineTransition> stored = new ArrayList<TimelineTransition>();
        for (TimelineTransition transition : transitions) {
            TimelineClip from = findVideoClip(transition.getFromClipId());
            TimelineClip to = findVideoClip(transition.getToClipId());
            if (from != null && to != null
                    && !"@capture".equals(from.getSource()) && !"@capture".equals(to.getSource())) {
                stored.add(transition);
            }
        }
        return Collections.unmodifiableList(stored);
    }

    /** Restores a saved transition without requiring the clips to be adjacent at load time. */
    public synchronized boolean restoreTransition(TimelineTransition transition) {
        if (transition == null) throw new IllegalArgumentException("Transition is required");
        TimelineClip from = findVideoClip(transition.getFromClipId());
        TimelineClip to = findVideoClip(transition.getToClipId());
        if (from == null || to == null || from == to
                || "@capture".equals(from.getSource()) || "@capture".equals(to.getSource())) return false;
        for (TimelineTransition existing : transitions) {
            if (existing.getFromClipId().equals(transition.getFromClipId())
                    && existing.getToClipId().equals(transition.getToClipId())) return false;
        }
        transitions.add(transition);
        return true;
    }

    private TimelineClip findVideoClip(String clipId) {
        for (TimelineTrack track : tracks) {
            if (!"video".equals(track.getId())) continue;
            for (TimelineClip clip : track.getClips()) {
                if (clip.getId().equals(clipId)) return clip;
            }
        }
        return null;
    }

    public synchronized TimelineTransition getTransition(String fromClipId, String toClipId) {
        for (TimelineTransition transition : transitions) {
            if (transition.getFromClipId().equals(fromClipId)
                    && transition.getToClipId().equals(toClipId)
                    && isValidTransition(transition)) return transition;
        }
        return null;
    }

    private boolean isValidTransition(TimelineTransition transition) {
        TimelineClip from = null;
        TimelineClip to = null;
        for (TimelineTrack track : tracks) {
            if (!"video".equals(track.getId())) continue;
            for (TimelineClip clip : track.getClips()) {
                if (clip.getId().equals(transition.getFromClipId())) from = clip;
                if (clip.getId().equals(transition.getToClipId())) to = clip;
            }
        }
        return from != null && to != null && from.getEnd() == to.getStart()
                && !"@capture".equals(from.getSource()) && !"@capture".equals(to.getSource())
                && transition.getDurationMillis() <= Math.min(from.getDuration(), to.getDuration());
    }

    /** Adds or replaces an explicitly selected transition between exactly adjacent video clips. */
    public synchronized void setTransition(String fromClipId, String toClipId,
                                           TimelineTransitionType type, long durationMillis) {
        TimelineClip from = null;
        TimelineClip to = null;
        for (TimelineTrack track : tracks) {
            if (!"video".equals(track.getId())) continue;
            for (TimelineClip clip : track.getClips()) {
                if (clip.getId().equals(fromClipId)) from = clip;
                if (clip.getId().equals(toClipId)) to = clip;
            }
        }
        if (from == null || to == null || from.getEnd() != to.getStart()) {
            throw new IllegalArgumentException("Choose two adjacent clips on the video track");
        }
        if ("@capture".equals(from.getSource()) || "@capture".equals(to.getSource())) {
            throw new IllegalArgumentException("Transitions are available between imported video clips only");
        }
        if (durationMillis > Math.min(from.getDuration(), to.getDuration())) {
            throw new IllegalArgumentException("Transition duration cannot exceed either clip");
        }
        TimelineTransition replacement = new TimelineTransition(fromClipId, toClipId, type, durationMillis);
        for (int i = 0; i < transitions.size(); i++) {
            TimelineTransition existing = transitions.get(i);
            if (existing.getFromClipId().equals(fromClipId)
                    || existing.getToClipId().equals(toClipId)) {
                transitions.remove(i--);
            }
        }
        transitions.add(replacement);
    }

    public synchronized boolean removeTransition(String fromClipId, String toClipId) {
        for (int i = 0; i < transitions.size(); i++) {
            TimelineTransition transition = transitions.get(i);
            if (transition.getFromClipId().equals(fromClipId)
                    && transition.getToClipId().equals(toClipId)) {
                transitions.remove(i);
                return true;
            }
        }
        return false;
    }

    public synchronized long duration() {
        long duration = markers.isEmpty() ? 0L : markers.get(markers.size() - 1).getTimeMillis();
        for (TimelineTrack track : tracks) {
            for (TimelineClip clip : track.getClips()) duration = Math.max(duration, clip.getEnd());
        }
        return duration;
    }
}
