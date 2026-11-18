package com.alpharip.staffmod.RecordingStudio.core;

import com.alpharip.staffmod.RecordingStudio.timeline.*;
import java.io.*;
import java.util.*;

/** Local project catalog and durable timeline persistence for Recording Studio. */
public final class RecordingStudioProjectStore {
    private final File directory;

    public RecordingStudioProjectStore(File directory) {
        if (directory == null) throw new IllegalArgumentException("Project directory is required");
        this.directory = directory;
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
            }
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
            Timeline timeline = new Timeline();
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
                    track.add(clip);
                }
                timeline.addTrack(track);
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

    /** Reads the timestamp of the last active recording checkpoint. */
    public synchronized long getRecoveryMarkedAt(String projectId) throws IOException {
        validateId(projectId);
        File marker = new File(directory, projectId + ".active");
        if (!marker.isFile()) return 0L;
        Properties data = new Properties();
        try (InputStream input = new BufferedInputStream(new FileInputStream(marker))) {
            data.load(input);
        }
        try {
            return Math.max(0L, Long.parseLong(data.getProperty("markedAt", "0")));
        } catch (NumberFormatException invalid) {
            throw new IOException("Invalid Recording Studio recovery marker", invalid);
        }
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
        File temp = new File(destination.getParentFile(), destination.getName() + ".tmp");
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
