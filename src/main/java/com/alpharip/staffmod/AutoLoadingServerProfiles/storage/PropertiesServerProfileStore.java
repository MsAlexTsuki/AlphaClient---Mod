package com.alpharip.staffmod.AutoLoadingServerProfiles.storage;

import com.alpharip.staffmod.AutoLoadingServerProfiles.model.ServerProfileBinding;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Local properties-file persistence. No network service or Supabase dependency is used. */
public final class PropertiesServerProfileStore implements ServerProfileStore {
    private final Path file;

    public PropertiesServerProfileStore(Path file) {
        if (file == null) throw new IllegalArgumentException("File path is required");
        this.file = file;
    }

    @Override
    public List<ServerProfileBinding> load() throws IOException {
        List<ServerProfileBinding> result = new ArrayList<ServerProfileBinding>();
        if (!Files.exists(file)) return result;

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(file)) {
            properties.load(input);
        }

        int count = parseCount(properties.getProperty("binding.count", "0"));
        for (int i = 0; i < count; i++) {
            String prefix = "binding." + i + ".";
            String address = properties.getProperty(prefix + "server");
            String profile = properties.getProperty(prefix + "profile");
            boolean enabled = Boolean.parseBoolean(properties.getProperty(prefix + "enabled", "true"));
            if (address == null || profile == null) {
                throw new IOException("Incomplete server-profile binding at index " + i);
            }
            try {
                result.add(new ServerProfileBinding(address, profile, enabled));
            } catch (IllegalArgumentException exception) {
                throw new IOException("Invalid server-profile binding at index " + i, exception);
            }
        }
        return result;
    }

    @Override
    public void save(List<ServerProfileBinding> bindings) throws IOException {
        if (bindings == null) throw new IllegalArgumentException("Bindings are required");
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);

        Properties properties = new Properties();
        properties.setProperty("binding.count", Integer.toString(bindings.size()));
        for (int i = 0; i < bindings.size(); i++) {
            ServerProfileBinding binding = bindings.get(i);
            String prefix = "binding." + i + ".";
            properties.setProperty(prefix + "server", binding.getServerAddress());
            properties.setProperty(prefix + "profile", binding.getProfileId());
            properties.setProperty(prefix + "enabled", Boolean.toString(binding.isEnabled()));
        }

        try (OutputStream output = Files.newOutputStream(file)) {
            properties.store(output, "Alpha Client - Auto Loading Server Profiles");
        }
    }

    private static int parseCount(String value) throws IOException {
        try {
            int count = Integer.parseInt(value);
            if (count < 0) throw new NumberFormatException("negative");
            return count;
        } catch (NumberFormatException exception) {
            throw new IOException("Invalid binding.count in " + "server profile store", exception);
        }
    }
}
