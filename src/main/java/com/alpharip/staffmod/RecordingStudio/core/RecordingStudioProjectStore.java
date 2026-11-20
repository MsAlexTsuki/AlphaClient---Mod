package com.alpharip.staffmod.RecordingStudio.core;

import com.alpharip.staffmod.RecordingStudio.timeline.*;
import com.alpharip.staffmod.RecordingStudio.scene.RecordingStudioScene;
import com.alpharip.staffmod.RecordingStudio.scene.RecordingStudioSource;
import java.io.*;
import java.util.*;

/** Local project catalog and durable timeline persistence for Recording Studio. */
public final class RecordingStudioProjectStore {
    private final File directory;

    public RecordingStudioProjectStore(File directory) {
        if (directory == null) throw new IllegalArgumentException("Project directory is required");
        this.directory = directory;
    }

    public synchronized void saveProject(RecordingProject project) throws IOException {
        if (project == null) throw new IllegalArgumentException("Project is required");
        project.save(directory);
    }

    public synchronized void save(RecordingProject project, Timeline timeline) throws IOException {
        if (project == null || timeline == null) throw new IllegalArgumentException("Project and timeline are required");
        project.save(directory);
        Properties data = new Properties();
        data.setProperty("format", "1");
        List<TimelineTrack> tracks = timeline.getTracks();
        data.setProperty("track.count", Integer.toString(tracks.size()));
        for (int i = 0; i < tracks.size(); i++) {
            TimelineTrack track = tracks.get(i);
            String prefix = "track." + i + ".";
            data.setProperty(prefix + "id", track.getId());
            data.setProperty(prefix + "name", track.getName());
            List<TimelineClip> clips = track.getClips();
            data.setProperty(prefix + "clip.count", Integer.toString(clips.size()));
            for (int j = 0; j < clips.size(); j++) {
                TimelineClip clip = clips.get(j);
                String cp = prefix + "clip." + j + ".";
                data.setProperty(cp + "id", clip.getId());
                data.setProperty(cp + "source", clip.getSource());
                data.setProperty(cp + "start", Long.toString(clip.getStart()));
                data.setProperty(cp + "duration", Long.toString(clip.getDuration()));
                data.setProperty(cp + "speed", Double.toString(clip.getSpeed()));
                data.setProperty(cp + "effect", clip.getEffect().name());
                data.setProperty(cp + "fadeInMillis", Long.toString(clip.getFadeInMillis()));
                data.setProperty(cp + "fadeOutMillis", Long.toString(clip.getFadeOutMillis()));
            }
        }
        List<RecordingStudioScene> scenes = timeline.getScenes();
        data.setProperty("scene.count", Integer.toString(scenes.size()));
        data.setProperty("activeSceneId", timeline.getActiveSceneId());
        for (int i = 0; i < scenes.size(); i++) {
            RecordingStudioScene scene = scenes.get(i);
            String prefix = "scene." + i + ".";
            data.setProperty(prefix + "id", scene.getId());
            data.setProperty(prefix + "name", scene.getName());
            List<RecordingStudioSource> sources = scene.getSources();
            data.setProperty(prefix + "source.count", Integer.toString(sources.size()));
            for (int j = 0; j < sources.size(); j++) {
                RecordingStudioSource source = sources.get(j);
                String sourcePrefix = prefix + "source." + j + ".";
                data.setProperty(sourcePrefix + "id", source.getId());
                data.setProperty(sourcePrefix + "name", source.getName());
                data.setProperty(sourcePrefix + "type", source.getType().name());
                data.setProperty(sourcePrefix + "enabled", Boolean.toString(source.isEnabled()));
                if (source.getType() == RecordingStudioSource.Type.MICROPHONE
                        || source.getType() == RecordingStudioSource.Type.GAME_AUDIO) {
                    data.setProperty(sourcePrefix + "audioDeviceKey", source.getAudioDeviceKey());
                }
                if (source.getType() == RecordingStudioSource.Type.DISPLAY) {
                    data.setProperty(sourcePrefix + "screenDeviceId", source.getScreenDeviceId());
                }
                if (source.getType() == RecordingStudioSource.Type.WEBCAM) data.setProperty(sourcePrefix + "webcamDeviceName", source.getWebcamDeviceName());
                data.setProperty(sourcePrefix + "xPercent", Integer.toString(source.getXPercent()));
                data.setProperty(sourcePrefix + "yPercent", Integer.toString(source.getYPercent()));
                data.setProperty(sourcePrefix + "widthPercent", Integer.toString(source.getWidthPercent()));
                data.setProperty(sourcePrefix + "heightPercent", Integer.toString(source.getHeightPercent()));
                data.setProperty(sourcePrefix + "cropLeftPercent", Integer.toString(source.getCropLeftPercent()));
                data.setProperty(sourcePrefix + "cropTopPercent", Integer.toString(source.getCropTopPercent()));
                data.setProperty(sourcePrefix + "cropRightPercent", Integer.toString(source.getCropRightPercent()));
                data.setProperty(sourcePrefix + "cropBottomPercent", Integer.toString(source.getCropBottomPercent()));
            }
        }

        List<TimelineTransition> transitions = timeline.getTransitions();
        data.setProperty("transition.count", Integer.toString(transitions.size()));
        for (int i = 0; i < transitions.size(); i++) {
            TimelineTransition transition = transitions.get(i);
            String prefix = "transition." + i + ".";
            data.setProperty(prefix + "fromClipId", transition.getFromClipId());
            data.setProperty(prefix + "toClipId", transition.getToClipId());
            data.setProperty(prefix + "type", transition.getType().name());
            data.setProperty(prefix + "durationMillis", Long.toString(transition.getDurationMillis()));
        }

        List<TimelineMarker> markers = timeline.getMarkers();
        data.setProperty("marker.count", Integer.toString(markers.size()));
        for (int i = 0; i < markers.size(); i++) {
            TimelineMarker marker = markers.get(i);
            String prefix = "marker." + i + ".";
            data.setProperty(prefix + "time", Long.toString(marker.getTimeMillis()));
            data.setProperty(prefix + "label", marker.getLabel());
            data.setProperty(prefix + "kind", marker.getKind().name());
        }
        writeAtomically(data, new File(directory, project.getId() + ".timeline"));
    }

    public synchronized Timeline loadTimeline(String projectId) throws IOException {
        validateId(projectId);
        File file = new File(directory, projectId + ".timeline");
        if (!file.isFile()) return defaultTimeline();
        Properties data = new Properties();
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            data.load(in);
        }
        if (!"1".equals(data.getProperty("format"))) throw new IOException("Unsupported timeline format");
        try {
            Timeline timeline = new Timeline(false);
            int trackCount = count(data, "track.count");
            for (int i = 0; i < trackCount; i++) {
                String prefix = "track." + i + ".";
                TimelineTrack track = new TimelineTrack(required(data, prefix + "id"), required(data, prefix + "name"));
                int clipCount = count(data, prefix + "clip.count");
                for (int j = 0; j < clipCount; j++) {
                    String cp = prefix + "clip." + j + ".";
                    TimelineClip clip = new TimelineClip(required(data, cp + "id"), required(data, cp + "source"),
                            Long.parseLong(required(data, cp + "start")), Long.parseLong(required(data, cp + "duration")));
                    clip.setSpeed(Double.parseDouble(required(data, cp + "speed")));
                    String savedEffect = data.getProperty(cp + "effect", TimelineVideoEffect.NONE.name());
                    clip.setEffect(TimelineVideoEffect.valueOf(savedEffect));
                    clip.setFadeInMillis(Long.parseLong(data.getProperty(cp + "fadeInMillis", "0")));
                    clip.setFadeOutMillis(Long.parseLong(data.getProperty(cp + "fadeOutMillis", "0")));
                    track.add(clip);
                }
                timeline.addTrack(track);
            }
            String savedTransitionCount = data.getProperty("transition.count");
            if (savedTransitionCount != null) {
                int transitionCount = count(data, "transition.count");
                for (int i = 0; i < transitionCount; i++) {
                    String prefix = "transition." + i + ".";
                    timeline.setTransition(required(data, prefix + "fromClipId"),
                            required(data, prefix + "toClipId"),
                            TimelineTransitionType.valueOf(required(data, prefix + "type")),
                            Long.parseLong(required(data, prefix + "durationMillis")));
                }
            }

            String savedSceneCount = data.getProperty("scene.count");
            if (savedSceneCount == null) {
                timeline.addScene(RecordingStudioScene.createDefault("main", "Main Scene"));
            } else {
                int sceneCount = count(data, "scene.count");
                for (int i = 0; i < sceneCount; i++) {
                    String prefix = "scene." + i + ".";
                    RecordingStudioScene scene = new RecordingStudioScene(
                            required(data, prefix + "id"), required(data, prefix + "name"));
                    int sourceCount = count(data, prefix + "source.count");
                    for (int j = 0; j < sourceCount; j++) {
                        String sourcePrefix = prefix + "source." + j + ".";
                        String enabledValue = required(data, sourcePrefix + "enabled");
                        if (!"true".equalsIgnoreCase(enabledValue) && !"false".equalsIgnoreCase(enabledValue)) {
                            throw new IOException("Invalid scene source enabled value");
                        }
                        RecordingStudioSource source = new RecordingStudioSource(
                                required(data, sourcePrefix + "id"),
                                required(data, sourcePrefix + "name"),
                                RecordingStudioSource.Type.valueOf(required(data, sourcePrefix + "type")),
                                Boolean.parseBoolean(enabledValue));
                        if (source.getType() == RecordingStudioSource.Type.MICROPHONE
                                || source.getType() == RecordingStudioSource.Type.GAME_AUDIO) {
                            source.setAudioDeviceKey(data.getProperty(sourcePrefix + "audioDeviceKey", ""));
                        }
                        if (source.getType() == RecordingStudioSource.Type.DISPLAY) source.setScreenDeviceId(data.getProperty(sourcePrefix + "screenDeviceId", ""));
                        if (source.getType() == RecordingStudioSource.Type.WEBCAM) source.setWebcamDeviceName(data.getProperty(sourcePrefix + "webcamDeviceName", ""));
                        source.setTransform(Integer.parseInt(data.getProperty(sourcePrefix + "xPercent", "0")),
                                Integer.parseInt(data.getProperty(sourcePrefix + "yPercent", "0")),
                                Integer.parseInt(data.getProperty(sourcePrefix + "widthPercent", "100")),
                                Integer.parseInt(data.getProperty(sourcePrefix + "heightPercent", "100")));
                        source.setCrop(Integer.parseInt(data.getProperty(sourcePrefix + "cropLeftPercent", "0")),
                                Integer.parseInt(data.getProperty(sourcePrefix + "cropTopPercent", "0")),
                                Integer.parseInt(data.getProperty(sourcePrefix + "cropRightPercent", "0")),
                                Integer.parseInt(data.getProperty(sourcePrefix + "cropBottomPercent", "0")));
                        scene.addSource(source);
                    }
                    timeline.addScene(scene);
                }
                if (sceneCount == 0) timeline.addScene(RecordingStudioScene.createDefault("main", "Main Scene"));
                String activeSceneId = data.getProperty("activeSceneId");
                if (activeSceneId != null && !activeSceneId.trim().isEmpty()
                        && !timeline.setActiveScene(activeSceneId)) {
                    throw new IOException("Saved active scene does not exist");
                }
            }

            int markerCount = count(data, "marker.count");
            for (int i = 0; i < markerCount; i++) {
                String prefix = "marker." + i + ".";
                timeline.addMarker(new TimelineMarker(Long.parseLong(required(data, prefix + "time")),
                        required(data, prefix + "label"), TimelineMarkerKind.valueOf(required(data, prefix + "kind"))));
            }
            return timeline;
        } catch (RuntimeException invalid) {
            throw new IOException("Invalid saved timeline data", invalid);
        }
    }

    public synchronized List<RecordingProject> list() throws IOException {
        if (!directory.exists()) return Collections.emptyList();
        File[] files = directory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) { return name.endsWith(".recording"); }
        });
        if (files == null) throw new IOException("Cannot read Recording Studio project directory");
        List<RecordingProject> projects = new ArrayList<RecordingProject>();
        for (File file : files) {
            String id = file.getName().substring(0, file.getName().length() - ".recording".length());
            try { projects.add(RecordingProject.load(directory, id)); }
            catch (IOException invalid) { /* Ignore invalid catalog entries; keep valid projects accessible. */ }
        }
        Collections.sort(projects, new Comparator<RecordingProject>() {
            public int compare(RecordingProject a, RecordingProject b) {
                return Long.compare(b.getCreatedAt(), a.getCreatedAt());
            }
        });
        return Collections.unmodifiableList(projects);
    }

    public synchronized RecordingProject loadProject(String id) throws IOException {
        validateId(id);
        return RecordingProject.load(directory, id);
    }


    /** Returns true when a previous process left an active-session marker behind. */
    public synchronized boolean hasRecoveryMarker(String projectId) throws IOException {
        validateId(projectId);
        return new File(directory, projectId + ".active").isFile();
    }

    /** Persist an active marker so an unclean process exit can be detected next time. */
    public synchronized void markRecordingActive(RecordingProject project) throws IOException {
        if (project == null) throw new IllegalArgumentException("Project is required");
        Properties data = new Properties();
        data.setProperty("format", "1");
        data.setProperty("projectId", project.getId());
        data.setProperty("projectName", project.getName());
        data.setProperty("markedAt", Long.toString(System.currentTimeMillis()));
        writeAtomically(data, new File(directory, project.getId() + ".active"));
    }

    /** Remove the marker only after the final project checkpoint has been saved. */
    public synchronized void clearRecordingActive(String projectId) throws IOException {
        validateId(projectId);
        File marker = new File(directory, projectId + ".active");
        if (marker.exists() && !marker.delete()) {
            throw new IOException("Cannot clear Recording Studio recovery marker");
        }
    }

    public synchronized void delete(String id) throws IOException {
        validateId(id);
        File metadata = new File(directory, id + ".recording");
        File timeline = new File(directory, id + ".timeline");
        if (metadata.exists() && !metadata.delete()) throw new IOException("Cannot delete project metadata");
        if (timeline.exists() && !timeline.delete()) throw new IOException("Cannot delete project timeline");
    }

    private static Timeline defaultTimeline() {
        Timeline timeline = new Timeline();
        timeline.addTrack(new TimelineTrack("video", "Video"));
        timeline.addTrack(new TimelineTrack("audio", "Audio"));
        return timeline;
    }

    private static int count(Properties data, String key) throws IOException {
        int value = Integer.parseInt(required(data, key));
        if (value < 0 || value > 10000) throw new IOException("Invalid collection size: " + key);
        return value;
    }

    private static String required(Properties data, String key) throws IOException {
        String value = data.getProperty(key);
        if (value == null) throw new IOException("Missing project field: " + key);
        return value;
    }

    private static void validateId(String id) throws IOException {
        if (id == null || !id.matches("[A-Za-z0-9_-]{1,80}")) throw new IOException("Invalid project id");
    }

    private static void writeAtomically(Properties data, File destination) throws IOException {
        File temp = new File(destination.getParentFile(),
                destination.getName() + "." + UUID.randomUUID().toString() + ".tmp");
        try {
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(temp))) {
                data.store(out, "Alpha Recording Studio timeline");
            }
            try {
                java.nio.file.Files.move(temp.toPath(), destination.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                        java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException unsupported) {
                java.nio.file.Files.move(temp.toPath(), destination.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            if (temp.exists() && !temp.delete()) temp.deleteOnExit();
        }
    }
}
