package com.alpharip.staffmod.store.addons;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/** Persists only local catalogue state; it never stores or installs executable add-on files. */
public final class AddonStoreStorage {
    private static final String SCHEMA = "1";
    private static final long MAX_FILE_BYTES = 1024L * 1024L;
    private final Path file;

    public AddonStoreStorage(Path file) {
        if (file == null || file.getFileName() == null)
            throw new IllegalArgumentException("A Store state file path is required.");
        this.file = file;
    }

    /** Reads states independently so they can be applied as trusted entries are registered. */
    public synchronized Map<String, AddonInstallState> loadStates() throws IOException {
        Map<String, AddonInstallState> states = new LinkedHashMap<String, AddonInstallState>();
        if (!Files.exists(file)) return states;
        long size = Files.size(file);
        if (size > MAX_FILE_BYTES) throw new IOException("Store state file exceeds the size limit.");
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(file)) {
            properties.load(input);
        } catch (IllegalArgumentException exception) {
            throw new IOException("Store state file contains malformed properties data.", exception);
        }
        if (!SCHEMA.equals(properties.getProperty("schema")))
            throw new IOException("Unsupported Store state format.");
        for (String key : properties.stringPropertyNames()) {
            if (!key.startsWith("state.")) continue;
            String id = key.substring("state.".length());
            if (id.isEmpty() || id.length() > 128) continue;
            try {
                states.put(id, AddonInstallState.valueOf(properties.getProperty(key)));
            } catch (IllegalArgumentException ignored) {
                // Ignore invalid individual states; valid entries can still be restored.
            }
        }
        return states;
    }

    public synchronized void save(AddonRegistry registry) throws IOException {
        save(registry, null);
    }

    /** Preserves saved states for catalogue entries that have not been registered this session yet. */
    public synchronized void save(AddonRegistry registry, Map<String, AddonInstallState> pending)
            throws IOException {
        if (registry == null) throw new IllegalArgumentException("Registry is required.");
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        Properties properties = new Properties();
        properties.setProperty("schema", SCHEMA);
        Map<String, AddonInstallState> allStates =
                new LinkedHashMap<String, AddonInstallState>(registry.getStatesSnapshot());
        if (pending != null) allStates.putAll(pending);
        for (Map.Entry<String, AddonInstallState> entry : allStates.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null)
                properties.setProperty("state." + entry.getKey(), entry.getValue().name());
        }
        Path temporary = file.resolveSibling(file.getFileName().toString() + ".tmp");
        try {
            try (OutputStream output = Files.newOutputStream(temporary)) {
                properties.store(output, "AlphaClient Store local state");
            }
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            Files.deleteIfExists(temporary);
            throw exception;
        }
    }

    public synchronized void delete() throws IOException {
        Files.deleteIfExists(file);
    }
}
