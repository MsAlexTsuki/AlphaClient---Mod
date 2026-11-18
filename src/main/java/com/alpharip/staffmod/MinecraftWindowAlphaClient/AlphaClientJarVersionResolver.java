package com.alpharip.staffmod.MinecraftWindowAlphaClient;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/** Reads the AlphaClient version from package metadata or the running JAR manifest. */
public final class AlphaClientJarVersionResolver {
    public String resolve() {
        Package clientPackage = MinecraftWindowAlphaClient.class.getPackage();
        if (clientPackage != null) {
            String packageVersion = clientPackage.getImplementationVersion();
            if (hasText(packageVersion)) return packageVersion.trim();
        }

        String manifestVersion = readVersionFromManifest();
        return hasText(manifestVersion) ? manifestVersion.trim() : "Unknown";
    }

    private String readVersionFromManifest() {
        try {
            if (!"jar".equalsIgnoreCase(MinecraftWindowAlphaClient.class
                    .getProtectionDomain().getCodeSource().getLocation().getProtocol())) {
                return null;
            }
            File jarFile = new File(MinecraftWindowAlphaClient.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            try (JarFile jar = new JarFile(jarFile)) {
                Manifest manifest = jar.getManifest();
                if (manifest == null) return null;
                Attributes attributes = manifest.getMainAttributes();
                String value = attributes.getValue("Implementation-Version");
                if (!hasText(value)) value = attributes.getValue("Specification-Version");
                return value;
            }
        } catch (IOException | URISyntaxException | SecurityException ex) {
            // A development run or a JAR without version metadata may not expose a version.
            return null;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
