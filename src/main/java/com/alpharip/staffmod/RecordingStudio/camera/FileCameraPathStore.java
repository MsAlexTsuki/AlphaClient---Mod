package com.alpharip.staffmod.RecordingStudio.camera;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/** Local, versioned camera-path persistence. No cloud or network operations. */
public final class FileCameraPathStore implements CameraPathStore {
    private final File root;

    public FileCameraPathStore(File root) {
        if (root == null) throw new IllegalArgumentException("root");
        this.root = root;
    }

    public void save(String id, CameraPath path) throws IOException {
        validate(id);
        if (path == null) throw new IllegalArgumentException("path");
        ensureRoot();

        Properties data = new Properties();
        data.setProperty("format", "1");
        data.setProperty("name", path.getName());
        List<CameraWaypoint> points = path.getWaypoints();
        data.setProperty("count", Integer.toString(points.size()));
        for (int i = 0; i < points.size(); i++) {
            CameraWaypoint point = points.get(i);
            String key = "point." + i + ".";
            CameraPose pose = point.getPose();
            data.setProperty(key + "id", point.getId());
            data.setProperty(key + "x", Double.toString(pose.x));
            data.setProperty(key + "y", Double.toString(pose.y));
            data.setProperty(key + "z", Double.toString(pose.z));
            data.setProperty(key + "yaw", Float.toString(pose.yaw));
            data.setProperty(key + "pitch", Float.toString(pose.pitch));
            data.setProperty(key + "roll", Float.toString(pose.roll));
            data.setProperty(key + "fov", Float.toString(pose.fov));
            data.setProperty(key + "duration", Long.toString(point.getDuration()));
            data.setProperty(key + "hold", Long.toString(point.getHold()));
            data.setProperty(key + "easing", point.getEasing());
        }

        File temporary = new File(root, id + "." + UUID.randomUUID().toString() + ".tmp");
        File destination = file(id);
        try (OutputStream output = new BufferedOutputStream(new FileOutputStream(temporary))) {
            data.store(output, "Recording Studio camera path");
        }
        try {
            Files.move(temporary.toPath(), destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException unsupported) {
            Files.move(temporary.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            if (temporary.exists()) temporary.deleteOnExit();
        }
    }

    public CameraPath load(String id) throws IOException {
        validate(id);
        File source = file(id);
        if (!source.isFile()) throw new FileNotFoundException(source.toString());

        Properties data = new Properties();
        try (InputStream input = new BufferedInputStream(new FileInputStream(source))) {
            data.load(input);
        }
        try {
            if (!"1".equals(data.getProperty("format"))) throw new IOException("Unsupported camera path format");
            CameraPath path = new CameraPath(required(data, "name"));
            int count = Integer.parseInt(required(data, "count"));
            if (count < 0 || count > 10000) throw new IOException("Invalid waypoint count");
            for (int i = 0; i < count; i++) {
                String key = "point." + i + ".";
                CameraPose pose = new CameraPose(
                        Double.parseDouble(required(data, key + "x")),
                        Double.parseDouble(required(data, key + "y")),
                        Double.parseDouble(required(data, key + "z")),
                        Float.parseFloat(required(data, key + "yaw")),
                        Float.parseFloat(required(data, key + "pitch")),
                        Float.parseFloat(required(data, key + "roll")),
                        Float.parseFloat(required(data, key + "fov")));
                path.add(new CameraWaypoint(required(data, key + "id"), pose,
                        Long.parseLong(required(data, key + "duration")),
                        Long.parseLong(required(data, key + "hold")),
                        required(data, key + "easing")));
            }
            return path;
        } catch (IllegalArgumentException invalid) {
            throw new IOException("Invalid camera path data: " + id, invalid);
        }
    }

    public List<String> listIds() {
        if (!root.isDirectory()) return Collections.emptyList();
        File[] files = root.listFiles();
        if (files == null) return Collections.emptyList();

        List<String> ids = new ArrayList<String>();
        for (File candidate : files) {
            String name = candidate.getName();
            if (candidate.isFile() && name.endsWith(".btpath")) {
                String id = name.substring(0, name.length() - ".btpath".length());
                if (id.matches("[A-Za-z0-9_-]{1,80}")) ids.add(id);
            }
        }
        Collections.sort(ids, new Comparator<String>() {
            public int compare(String first, String second) { return first.compareToIgnoreCase(second); }
        });
        return Collections.unmodifiableList(ids);
    }

    public boolean delete(String id) throws IOException {
        validate(id);
        File source = file(id);
        return !source.exists() || source.delete();
    }

    private void ensureRoot() throws IOException {
        if ((!root.exists() && !root.mkdirs()) || !root.isDirectory()) {
            throw new IOException("Cannot create camera path directory");
        }
    }

    private File file(String id) { return new File(root, id + ".btpath"); }

    private static void validate(String id) throws IOException {
        if (id == null || !id.matches("[A-Za-z0-9_-]{1,80}")) {
            throw new IOException("Invalid camera path id");
        }
    }

    private static String required(Properties data, String key) throws IOException {
        String value = data.getProperty(key);
        if (value == null) throw new IOException("Missing camera path property: " + key);
        return value;
    }
}
