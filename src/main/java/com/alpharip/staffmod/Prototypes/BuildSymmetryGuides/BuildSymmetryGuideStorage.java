package com.alpharip.staffmod.buildsymmetryguides;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/** Local properties-file persistence; this prototype has no remote storage. */
public final class BuildSymmetryGuideStorage {
    private BuildSymmetryGuideStorage() { }

    public static void load(File file, BuildSymmetryGuideConfig config) {
        if (file == null || config == null || !file.isFile()) return;
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(file)) {
            properties.load(input);
            config.setEnabled(readBoolean(properties, "enabled", config.isEnabled()));
            config.setAxis(SymmetryAxis.fromId(readInt(properties, "axis",
                    config.getAxis().getId())));
            config.setOrigin(readInt(properties, "originX", config.getOriginX()),
                    readInt(properties, "originZ", config.getOriginZ()));
            config.setShowMirrorPreview(readBoolean(properties, "showMirrorPreview",
                    config.isShowMirrorPreview()));
            config.setShowAxisLines(readBoolean(properties, "showAxisLines",
                    config.isShowAxisLines()));
            config.setShowConnectionLines(readBoolean(properties, "showConnectionLines",
                    config.isShowConnectionLines()));
            config.setGuideDistance(readInt(properties, "guideDistance",
                    config.getGuideDistance()));
            config.setOpacity(readFloat(properties, "opacity", config.getOpacity()));
            config.setColor(SymmetryGuideColor.fromId(readInt(properties, "color",
                    config.getColor().getId())));
        } catch (Exception exception) {
            System.out.println("[AlphaClient] Could not load Build Symmetry Guides settings: "
                    + exception.getMessage());
        }
    }

    public static boolean save(File file, BuildSymmetryGuideConfig config) {
        if (file == null || config == null) return false;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return false;
        Properties properties = new Properties();
        properties.setProperty("enabled", String.valueOf(config.isEnabled()));
        properties.setProperty("axis", String.valueOf(config.getAxis().getId()));
        properties.setProperty("originX", String.valueOf(config.getOriginX()));
        properties.setProperty("originZ", String.valueOf(config.getOriginZ()));
        properties.setProperty("showMirrorPreview", String.valueOf(config.isShowMirrorPreview()));
        properties.setProperty("showAxisLines", String.valueOf(config.isShowAxisLines()));
        properties.setProperty("showConnectionLines", String.valueOf(config.isShowConnectionLines()));
        properties.setProperty("guideDistance", String.valueOf(config.getGuideDistance()));
        properties.setProperty("opacity", String.valueOf(config.getOpacity()));
        properties.setProperty("color", String.valueOf(config.getColor().getId()));

        File temporary = new File(parent, file.getName() + ".tmp");
        try {
            try (FileOutputStream output = new FileOutputStream(temporary)) {
                properties.store(output, "AlphaClient Build Symmetry Guides");
                output.flush();
            }
            try {
                Files.move(temporary.toPath(), file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary.toPath(), file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException exception) {
            System.out.println("[AlphaClient] Could not save Build Symmetry Guides settings: "
                    + exception.getMessage());
            if (temporary.exists()) temporary.delete();
            return false;
        }
    }

    private static boolean readBoolean(Properties properties, String key, boolean fallback) {
        String value = properties.getProperty(key);
        return value == null ? fallback : Boolean.parseBoolean(value);
    }

    private static int readInt(Properties properties, String key, int fallback) {
        try { return Integer.parseInt(properties.getProperty(key)); }
        catch (Exception ignored) { return fallback; }
    }

    private static float readFloat(Properties properties, String key, float fallback) {
        try { return Float.parseFloat(properties.getProperty(key)); }
        catch (Exception ignored) { return fallback; }
    }
}
